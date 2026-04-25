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

    private String provider = "minio";
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private Integer uploadExpireSeconds = 900;
}
