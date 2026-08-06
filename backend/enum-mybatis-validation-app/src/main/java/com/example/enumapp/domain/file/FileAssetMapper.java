package com.example.enumapp.domain.file;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FileAssetMapper {

    int insert(FileAsset asset);

    FileAsset findById(@Param("id") Long id);

    int updateStatusAndOwner(
            @Param("id") Long id,
            @Param("status") FileAssetStatus status,
            @Param("ownerRef") String ownerRef
    );

    int deleteById(@Param("id") Long id);

    List<FileAsset> findTempOlderThan(@Param("threshold") LocalDateTime threshold);
}
