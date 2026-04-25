package com.community.backend.vo.notification;

import com.community.backend.vo.user.UserSummaryVo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 通知列表项返回体。
 */
public class NotificationItemVo {

    private Long id;
    private Long actorId;
    private String type;
    private String targetType;
    private Long targetId;
    private String title;
    private String content;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private UserSummaryVo actor;
}
