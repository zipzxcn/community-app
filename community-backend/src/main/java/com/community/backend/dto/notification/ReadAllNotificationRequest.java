package com.community.backend.dto.notification;

import lombok.Data;

@Data
/**
 * 批量已读通知请求参数：可按通知类型筛选。
 */
public class ReadAllNotificationRequest {

    private String type;
}
