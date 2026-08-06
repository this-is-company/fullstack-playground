package com.example.enumapp.web.dto;

import com.example.enumapp.domain.file.FileAsset;
import com.example.enumapp.domain.file.FileAssetStatus;

import java.time.LocalDateTime;

public class FileAssetResponse {

    private Long id;
    private String originalName;
    private String contentType;
    private long sizeBytes;
    private FileAssetStatus status;
    private String ownerRef;
    private LocalDateTime createdAt;

    public static FileAssetResponse from(FileAsset asset) {
        FileAssetResponse r = new FileAssetResponse();
        r.id = asset.getId();
        r.originalName = asset.getOriginalName();
        r.contentType = asset.getContentType();
        r.sizeBytes = asset.getSizeBytes();
        r.status = asset.getStatus();
        r.ownerRef = asset.getOwnerRef();
        r.createdAt = asset.getCreatedAt();
        return r;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public FileAssetStatus getStatus() {
        return status;
    }

    public String getOwnerRef() {
        return ownerRef;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
