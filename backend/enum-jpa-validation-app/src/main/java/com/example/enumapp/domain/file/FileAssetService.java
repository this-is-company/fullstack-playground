package com.example.enumapp.domain.file;

import com.example.enumapp.web.ApiMessageCodes;
import com.example.enumapp.web.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 파일 수명주기:
 * <ol>
 *   <li>선택 직후 {@link #uploadTemp} → 디스크 저장 + DB status=TEMP</li>
 *   <li>본문 저장 성공 시 {@link #commit} → status=USED + ownerRef</li>
 *   <li>저장 취소/실패 시 {@link #discard} → TEMP 물리파일+메타 삭제</li>
 *   <li>교체 {@link #replace}: 새 TEMP 업로드 후 commit 시 옛 USED 파일 삭제</li>
 *   <li>{@link #cleanupOrphans}: 오래된 TEMP 정리</li>
 * </ol>
 * 교체 시 새 파일이 "자동으로 옛 경로로 옮겨지지" 않는다. 앱이 명시적으로
 * 새 파일을 commit 하고 옛 파일을 삭제해야 한다.
 */
@Service
public class FileAssetService {

    private final FileAssetRepository fileAssetRepository;
    private final Path rootDir;

    public FileAssetService(
            FileAssetRepository fileAssetRepository,
            @Value("${app.upload.dir:./uploads}") String uploadDir
    ) throws IOException {
        this.fileAssetRepository = fileAssetRepository;
        this.rootDir = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(rootDir);
    }

    @Transactional
    public FileAsset uploadTemp(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "file is required");
        }
        String storedName = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
        Path target = rootDir.resolve(storedName);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("failed to store file", e);
        }

        FileAsset asset = new FileAsset();
        asset.setOriginalName(file.getOriginalFilename());
        asset.setStoredName(storedName);
        asset.setContentType(file.getContentType());
        asset.setSizeBytes(file.getSize());
        asset.setStatus(FileAssetStatus.TEMP);
        asset.setCreatedAt(LocalDateTime.now());
        return fileAssetRepository.save(asset);
    }

    @Transactional(readOnly = true)
    public FileAsset get(Long id) {
        return fileAssetRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(ApiMessageCodes.FILE_NOT_FOUND, "file not found: " + id));
    }

    /** 본문 저장 성공 후 TEMP → USED */
    @Transactional
    public FileAsset commit(Long id, String ownerRef) {
        FileAsset asset = get(id);
        if (asset.getStatus() != FileAssetStatus.TEMP && asset.getStatus() != FileAssetStatus.USED) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "invalid file status");
        }
        asset.setStatus(FileAssetStatus.USED);
        asset.setOwnerRef(ownerRef);
        return fileAssetRepository.save(asset);
    }

    /**
     * 파일 교체: 새 TEMP 를 commit 한 뒤, 이전 USED 파일을 디스크+DB 에서 삭제한다.
     * 새 commit 전에 실패하면 옛 파일은 그대로 남는다 (안전).
     */
    @Transactional
    public FileAsset replace(Long oldFileId, Long newTempFileId, String ownerRef) {
        FileAsset oldAsset = get(oldFileId);
        FileAsset newAsset = get(newTempFileId);
        if (newAsset.getStatus() != FileAssetStatus.TEMP) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "new file must be TEMP");
        }
        newAsset.setStatus(FileAssetStatus.USED);
        newAsset.setOwnerRef(ownerRef);
        fileAssetRepository.save(newAsset);
        deletePhysicalAndRow(oldAsset);
        return get(newTempFileId);
    }

    /** 저장 안 함 / 사용자가 선택 취소 → TEMP 삭제 */
    @Transactional
    public void discard(Long id) {
        FileAsset asset = get(id);
        if (asset.getStatus() != FileAssetStatus.TEMP) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "only TEMP files can be discarded");
        }
        deletePhysicalAndRow(asset);
    }

    @Transactional
    public List<Long> cleanupOrphans(int olderThanHours) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(olderThanHours);
        List<FileAsset> orphans = fileAssetRepository.findByStatusAndCreatedAtBefore(FileAssetStatus.TEMP, threshold);
        List<Long> deleted = new ArrayList<>();
        for (FileAsset orphan : orphans) {
            deletePhysicalAndRow(orphan);
            deleted.add(orphan.getId());
        }
        return deleted;
    }

    public Path resolvePath(FileAsset asset) {
        return rootDir.resolve(asset.getStoredName());
    }

    private void deletePhysicalAndRow(FileAsset asset) {
        try {
            Files.deleteIfExists(rootDir.resolve(asset.getStoredName()));
        } catch (IOException e) {
            throw new IllegalStateException("failed to delete file: " + asset.getStoredName(), e);
        }
        fileAssetRepository.deleteById(asset.getId());
    }

    private static String sanitize(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
