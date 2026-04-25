package com.community.backend.vo.file;

import lombok.Data;

import java.util.Map;

@Data
/**
 * 上传凭证返回体：包含预签名 URL 与上传头信息。
 */
public class UploadTokenVo {

    private String provider;
    private String uploadUrl;
    private String bucketName;
    private String objectKey;
    private Map<String, String> headers;
}
