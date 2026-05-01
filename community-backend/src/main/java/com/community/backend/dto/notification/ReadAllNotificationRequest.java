package com.community.backend.dto.notification;

import lombok.Data;

@Data
/**
 * 批量已读通知请求参数：可按通知类型筛选。
 */
public class ReadAllNotificationRequest {

    /**
     * 类型字段，用于区分通知类型、消息类型等。
     */
    private String type;
}
