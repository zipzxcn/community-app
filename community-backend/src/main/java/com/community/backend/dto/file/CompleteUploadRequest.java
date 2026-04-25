package com.community.backend.dto.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * 上传完成回调参数：用于落库文件元数据。
 */
public class CompleteUploadRequest {

    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    private Long bizId;

    @NotBlank(message = "bucket 不能为空")
    private String bucketName;

    @NotBlank(message = "objectKey 不能为空")
    private String objectKey;

    @NotBlank(message = "访问地址不能为空")
    private String accessUrl;

    @NotBlank(message = "原文件名不能为空")
    private String originalName;

    @NotBlank(message = "MIME 类型不能为空")
    private String mimeType;

    @NotNull(message = "文件大小不能为空")
    private Long sizeBytes;
}
