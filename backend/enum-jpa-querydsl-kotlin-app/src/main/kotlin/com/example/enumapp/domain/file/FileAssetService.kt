package com.example.enumapp.domain.file

import com.example.enumapp.web.ApiMessageCodes
import com.example.enumapp.web.BusinessException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.UUID

/**
 * 파일 수명주기:
 * 1. 선택 직후 [uploadTemp] → 디스크 저장 + DB status=TEMP
 * 2. 본문 저장 성공 시 [commit] → status=USED + ownerRef
 * 3. 저장 취소/실패 시 [discard] → TEMP 물리파일+메타 삭제
 * 4. 교체 [replace]: 새 TEMP 업로드 후 commit 시 옛 USED 파일 삭제
 * 5. [cleanupOrphans]: 오래된 TEMP 정리
 *
 * 교체 시 새 파일이 "자동으로 옛 경로로 옮겨지지" 않는다. 앱이 명시적으로
 * 새 파일을 commit 하고 옛 파일을 삭제해야 한다.
 */
@Service
class FileAssetService(
    private val fileAssetRepository: FileAssetRepository,
    @Value("\${app.upload.dir:./uploads}") uploadDir: String,
) {
    private val rootDir: Path = Path.of(uploadDir).toAbsolutePath().normalize()

    init {
        Files.createDirectories(rootDir)
    }

    @Transactional
    fun uploadTemp(file: MultipartFile?): FileAsset {
        if (file == null || file.isEmpty) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "file is required")
        }
        val storedName = UUID.randomUUID().toString() + "_" + sanitize(file.originalFilename)
        val target = rootDir.resolve(storedName)
        try {
            file.transferTo(target)
        } catch (e: IOException) {
            throw IllegalStateException("failed to store file", e)
        }

        val asset = FileAsset().apply {
            originalName = file.originalFilename
            this.storedName = storedName
            contentType = file.contentType
            sizeBytes = file.size
            status = FileAssetStatus.TEMP
            createdAt = LocalDateTime.now()
        }
        return fileAssetRepository.save(asset)
    }

    @Transactional(readOnly = true)
    fun get(id: Long): FileAsset =
        fileAssetRepository.findById(id).orElseThrow {
            BusinessException.notFound(ApiMessageCodes.FILE_NOT_FOUND, "file not found: $id")
        }

    /** 본문 저장 성공 후 TEMP → USED */
    @Transactional
    fun commit(id: Long, ownerRef: String): FileAsset {
        val asset = get(id)
        if (asset.status != FileAssetStatus.TEMP && asset.status != FileAssetStatus.USED) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "invalid file status")
        }
        asset.status = FileAssetStatus.USED
        asset.ownerRef = ownerRef
        return get(id)
    }

    /**
     * 파일 교체: 새 TEMP 를 commit 한 뒤, 이전 USED 파일을 디스크+DB 에서 삭제한다.
     * 새 commit 전에 실패하면 옛 파일은 그대로 남는다 (안전).
     */
    @Transactional
    fun replace(oldFileId: Long, newTempFileId: Long, ownerRef: String): FileAsset {
        val oldAsset = get(oldFileId)
        val newAsset = get(newTempFileId)
        if (newAsset.status != FileAssetStatus.TEMP) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "new file must be TEMP")
        }
        newAsset.status = FileAssetStatus.USED
        newAsset.ownerRef = ownerRef
        deletePhysicalAndRow(oldAsset)
        return get(newTempFileId)
    }

    /** 저장 안 함 / 사용자가 선택 취소 → TEMP 삭제 */
    @Transactional
    fun discard(id: Long) {
        val asset = get(id)
        if (asset.status != FileAssetStatus.TEMP) {
            throw BusinessException.badRequest(ApiMessageCodes.BAD_REQUEST, "only TEMP files can be discarded")
        }
        deletePhysicalAndRow(asset)
    }

    @Transactional
    fun cleanupOrphans(olderThanHours: Int): List<Long> {
        val threshold = LocalDateTime.now().minusHours(olderThanHours.toLong())
        val orphans = fileAssetRepository.findByStatusAndCreatedAtBefore(FileAssetStatus.TEMP, threshold)
        val deleted = ArrayList<Long>()
        for (orphan in orphans) {
            deletePhysicalAndRow(orphan)
            deleted.add(orphan.id!!)
        }
        return deleted
    }

    fun resolvePath(asset: FileAsset): Path = rootDir.resolve(asset.storedName!!)

    private fun deletePhysicalAndRow(asset: FileAsset) {
        try {
            Files.deleteIfExists(rootDir.resolve(asset.storedName!!))
        } catch (e: IOException) {
            throw IllegalStateException("failed to delete file: ${asset.storedName}", e)
        }
        fileAssetRepository.deleteById(asset.id!!)
    }

    companion object {
        private fun sanitize(name: String?): String {
            if (name.isNullOrBlank()) {
                return "file"
            }
            return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        }
    }
}
