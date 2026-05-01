package com.community.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 对象存储配置项映射（app.storage.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /**
     * 对象存储实现类型，当前默认 minio。
     */
    private String provider = "minio";
    /**
     * MinIO/S3 访问端点。
     */
    private String endpoint;
    /**
     * 对象存储访问密钥 Access Key。
     */
    private String accessKey;
    /**
     * 对象存储访问密钥 Secret Key。
     */
    private String secretKey;
    /**
     * 业务默认使用的存储桶名称。
     */
    private String bucket;
    /**
     * 预签名上传地址有效期秒数。
     */
    private Integer uploadExpireSeconds = 900;
}
