package com.community.backend.config;

import com.community.backend.common.error.BizException;
import com.community.backend.common.error.ErrorCode;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * MinIO 客户端配置：仅当 storage.provider=minio 时生效。
 */
@Configuration
public class MinioClientConfig {

    /**
     * 初始化 MinioClient，并在启动时校验关键配置项。
     */
    @Bean
    @ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "minio")
    public MinioClient minioClient(StorageProperties storageProperties) {
        if (!StringUtils.hasText(storageProperties.getEndpoint())
                || !StringUtils.hasText(storageProperties.getAccessKey())
                || !StringUtils.hasText(storageProperties.getSecretKey())) {
            throw BizException.of(ErrorCode.STORAGE_CONFIG_INCOMPLETE);
        }
        return MinioClient.builder()
                .endpoint(storageProperties.getEndpoint())
                .credentials(storageProperties.getAccessKey(), storageProperties.getSecretKey())
                .build();
    }
}
