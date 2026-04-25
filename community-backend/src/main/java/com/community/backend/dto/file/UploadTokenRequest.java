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

    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255位")
    private String fileName;

    @NotBlank(message = "文件类型不能为空")
    private String contentType;

    @NotNull(message = "文件大小不能为空")
    private Long size;
}
