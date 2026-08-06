package com.example.enumapp.domain.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FileAssetRepository extends JpaRepository<FileAsset, Long> {

    List<FileAsset> findByStatusAndCreatedAtBefore(FileAssetStatus status, LocalDateTime threshold);
}
