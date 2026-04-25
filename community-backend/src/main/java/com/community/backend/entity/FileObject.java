package com.community.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("file_object")
/**
 * 文件对象实体：记录上传文件元数据与业务绑定信息。
 */
public class FileObject {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long uploaderId;
    private String storageProvider;
    private String bucketName;
    private String objectKey;
    private String accessUrl;
    private String originalName;
    private String ext;
    private String mimeType;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
    private String checksumMd5;
    private String bizType;
    private Long bizId;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
