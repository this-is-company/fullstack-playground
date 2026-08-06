package com.example.enumapp.domain.file

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface FileAssetRepository : JpaRepository<FileAsset, Long> {

    fun findByStatusAndCreatedAtBefore(status: FileAssetStatus, threshold: LocalDateTime): List<FileAsset>
}
