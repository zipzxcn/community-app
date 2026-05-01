package com.community.backend.dto.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * 上传完成回调参数：用于落库文件元数据。
 */
public class CompleteUploadRequest {

    /**
     * 业务场景类型，例如 avatar、post-cover、chat-image。
     */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /**
     * 业务场景关联主键，例如帖子 ID、用户 ID。
     */
    private Long bizId;

    /**
     * 对象存储桶名称。
     */
    @NotBlank(message = "bucket 不能为空")
    private String bucketName;

    /**
     * 对象存储中的对象键，相当于文件在桶内的唯一路径。
     */
    @NotBlank(message = "objectKey 不能为空")
    private String objectKey;

    /**
     * 对象存储可访问地址，后端落库后供前端展示。
     */
    @NotBlank(message = "访问地址不能为空")
    private String accessUrl;

    /**
     * 原始文件名，便于前端展示给用户。
     */
    @NotBlank(message = "原文件名不能为空")
    private String originalName;

    /**
     * 文件 MIME 类型，用于浏览器展示或下载。
     */
    @NotBlank(message = "MIME 类型不能为空")
    private String mimeType;

    /**
     * 文件大小，单位字节。
     */
    @NotNull(message = "文件大小不能为空")
    private Long sizeBytes;
}
