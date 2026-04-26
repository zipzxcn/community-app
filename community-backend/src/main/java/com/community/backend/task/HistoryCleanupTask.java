package com.community.backend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.backend.config.HistoryCleanupProperties;
import com.community.backend.entity.UserBrowseHistory;
import com.community.backend.mapper.UserBrowseHistoryMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 定时清理过期浏览历史。
 */
@Component
public class HistoryCleanupTask {

    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final HistoryCleanupProperties historyCleanupProperties;

    public HistoryCleanupTask(UserBrowseHistoryMapper userBrowseHistoryMapper,
                              HistoryCleanupProperties historyCleanupProperties) {
        this.userBrowseHistoryMapper = userBrowseHistoryMapper;
        this.historyCleanupProperties = historyCleanupProperties;
    }

    @Scheduled(cron = "${app.history.cleanup.cron:0 15 3 * * *}")
    public void cleanupExpiredHistories() {
        if (!historyCleanupProperties.isEnabled()) {
            return;
        }
        LocalDateTime expireBefore = LocalDateTime.now().minusDays(Math.max(historyCleanupProperties.getRetentionDays(), 1));
        userBrowseHistoryMapper.delete(new LambdaQueryWrapper<UserBrowseHistory>()
                .lt(UserBrowseHistory::getLastViewedAt, expireBefore));
    }
}
