package com.community.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 浏览历史定时清理配置（app.history.cleanup.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.history.cleanup")
public class HistoryCleanupProperties {

    private boolean enabled = true;
    private int retentionDays = 180;
    private String cron = "0 15 3 * * *";
}
