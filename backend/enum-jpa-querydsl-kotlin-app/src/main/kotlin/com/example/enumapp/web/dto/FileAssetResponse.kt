package com.example.enumapp.web.dto

import com.example.enumapp.domain.file.FileAsset
import com.example.enumapp.domain.file.FileAssetStatus
import java.time.LocalDateTime

class FileAssetResponse {
    var id: Long? = null
    var originalName: String? = null
    var contentType: String? = null
    var sizeBytes: Long = 0
    var status: FileAssetStatus? = null
    var ownerRef: String? = null
    var createdAt: LocalDateTime? = null

    companion object {
        fun from(asset: FileAsset): FileAssetResponse =
            FileAssetResponse().apply {
                id = asset.id
                originalName = asset.originalName
                contentType = asset.contentType
                sizeBytes = asset.sizeBytes
                status = asset.status
                ownerRef = asset.ownerRef
                createdAt = asset.createdAt
            }
    }
}
