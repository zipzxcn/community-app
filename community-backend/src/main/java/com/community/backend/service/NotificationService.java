package com.community.backend.service;

import com.community.backend.common.api.PageResponse;
import com.community.backend.vo.notification.NotificationItemVo;
import com.community.backend.vo.notification.NotificationUnreadVo;

public interface NotificationService {

    void create(Long receiverId, Long actorId, String type, String targetType, Long targetId, String title, String content);

    PageResponse<NotificationItemVo> list(Long currentUserId,
                                          String type,
                                          String targetType,
                                          Boolean isRead,
                                          String keyword,
                                          Long page,
                                          Long size);

    NotificationUnreadVo unreadCount(Long currentUserId);

    void markRead(Long currentUserId, Long notificationId);

    void markAllRead(Long currentUserId, String type);
}
