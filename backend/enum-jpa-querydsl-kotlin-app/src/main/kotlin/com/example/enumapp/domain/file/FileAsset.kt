package com.example.enumapp.domain.file

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "kqdsl_demo_files")
open class FileAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null

    @Column(name = "original_name", nullable = false, length = 255)
    open var originalName: String? = null

    @Column(name = "stored_name", nullable = false, length = 255)
    open var storedName: String? = null

    @Column(name = "content_type", length = 100)
    open var contentType: String? = null

    @Column(name = "size_bytes", nullable = false)
    open var sizeBytes: Long = 0

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    open var status: FileAssetStatus? = null

    @Column(name = "owner_ref", length = 100)
    open var ownerRef: String? = null

    @Column(name = "created_at", nullable = false)
    open var createdAt: LocalDateTime? = null
}
