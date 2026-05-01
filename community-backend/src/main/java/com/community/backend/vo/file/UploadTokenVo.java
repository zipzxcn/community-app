package com.community.backend.vo.file;

import lombok.Data;

import java.util.Map;

@Data
/**
 * 上传凭证返回体：包含预签名 URL 与上传头信息。
 */
public class UploadTokenVo {

    /**
     * 存储提供方标识。
     */
    private String provider;
    /**
     * 预签名上传地址，前端可直接 PUT 文件内容。
     */
    private String uploadUrl;
    /**
     * 对象存储桶名称。
     */
    private String bucketName;
    /**
     * 对象存储中的对象键，相当于文件在桶内的唯一路径。
     */
    private String objectKey;
    /**
     * 对象存储要求附带的请求头，例如 Content-Type。
     */
    private Map<String, String> headers;
}
