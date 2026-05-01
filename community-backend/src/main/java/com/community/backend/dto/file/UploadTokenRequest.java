package com.community.backend.dto.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * 上传凭证请求参数：文件名、MIME、业务类型等。
 */
public class UploadTokenRequest {

    /**
     * 业务场景类型，例如 avatar、post-cover、chat-image。
     */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255位")
    private String fileName;

    /**
     * HTTP 内容类型。
     */
    @NotBlank(message = "文件类型不能为空")
    private String contentType;

    /**
     * 每页条数，用于限制单次查询返回量。
     */
    @NotNull(message = "文件大小不能为空")
    private Long size;
}
