package com.community.backend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.backend.config.HistoryCleanupProperties;
import com.community.backend.entity.UserBrowseHistory;
import com.community.backend.mapper.UserBrowseHistoryMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 浏览历史清理任务：
 * - 通过 Spring @Scheduled 定时触发。
 * - 解决浏览历史长期累积导致数据膨胀的问题。
 * - 保留天数和 cron 都由配置驱动，方便不同环境灵活调整。
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
        // 至少按 1 天兜底，避免错误配置 0 或负数时把所有数据一次性清空。
        LocalDateTime expireBefore = LocalDateTime.now().minusDays(Math.max(historyCleanupProperties.getRetentionDays(), 1));
        userBrowseHistoryMapper.delete(new LambdaQueryWrapper<UserBrowseHistory>()
                .lt(UserBrowseHistory::getLastViewedAt, expireBefore));
    }
}
