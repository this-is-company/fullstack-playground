package com.example.enumapp.web

import com.example.enumapp.domain.file.FileAssetService
import com.example.enumapp.web.dto.FileAssetResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * 파일 업로드 수명주기 데모.
 *
 * 1) 파일 선택 직후 → POST /temp (TEMP)
 * 2) 폼 저장 성공 → POST /{id}/commit?ownerRef=order:123 (USED)
 * 3) 폼 취소/실패 → DELETE /{id} (TEMP 삭제)
 * 4) 파일 교체 → 새 TEMP 업로드 후 POST /replace?oldId=&newId=&ownerRef=
 *    (새 파일 USED, 옛 파일 물리+메타 삭제. 자동 이동 아님)
 * 5) 방치된 TEMP → POST /cleanup-orphans
 */
@Tag(name = "File Upload Demo", description = "TEMP→USED commit / discard / replace / orphan cleanup")
@RestController
@RequestMapping("/api/demo/files")
class FileUploadDemoController(
    private val fileAssetService: FileAssetService,
) {

    @Operation(summary = "임시 업로드", description = "선택 직후 바로 업로드. status=TEMP. 본문 저장 전.")
    @PostMapping("/temp", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun uploadTemp(@RequestParam("file") file: MultipartFile): FileAssetResponse =
        FileAssetResponse.from(fileAssetService.uploadTemp(file))

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): FileAssetResponse =
        FileAssetResponse.from(fileAssetService.get(id))

    @Operation(summary = "확정(commit)", description = "본문 저장 성공 후 TEMP→USED + ownerRef 연결")
    @PostMapping("/{id}/commit")
    fun commit(
        @PathVariable id: Long,
        @RequestParam ownerRef: String,
    ): FileAssetResponse =
        FileAssetResponse.from(fileAssetService.commit(id, ownerRef))

    @Operation(summary = "임시 삭제(discard)", description = "저장 안 함/선택 취소 시 TEMP 파일 삭제")
    @DeleteMapping("/{id}")
    fun discard(@PathVariable id: Long) {
        fileAssetService.discard(id)
    }

    @Operation(
        summary = "파일 교체",
        description = "새 TEMP 를 USED 로 commit 하고, 이전 USED 파일을 디스크+DB에서 삭제한다. "
            + "파일이 자동으로 옮겨지지 않음 — 명시적 replace.",
    )
    @PostMapping("/replace")
    fun replace(
        @RequestParam oldId: Long,
        @RequestParam newId: Long,
        @RequestParam ownerRef: String,
    ): FileAssetResponse =
        FileAssetResponse.from(fileAssetService.replace(oldId, newId, ownerRef))

    @Operation(summary = "고아 TEMP 정리", description = "N시간 이상 된 TEMP 물리파일+메타 삭제")
    @PostMapping("/cleanup-orphans")
    fun cleanup(@RequestParam(defaultValue = "24") olderThanHours: Int): Map<String, Any> {
        val deleted = fileAssetService.cleanupOrphans(olderThanHours)
        return mapOf("deletedIds" to deleted, "olderThanHours" to olderThanHours)
    }
}
