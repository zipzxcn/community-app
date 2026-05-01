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

    /**
     * 是否启用浏览历史定时清理任务。
     */
    private boolean enabled = true;
    /**
     * 保留最近多少天的浏览记录。
     */
    private int retentionDays = 180;
    /**
     * 历史清理任务执行时间表达式。
     */
    private String cron = "0 15 3 * * *";
}
