package com.community.backend.vo.notification;

import com.community.backend.vo.user.UserSummaryVo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 通知列表项返回体。
 */
public class NotificationItemVo {

    /**
     * 通知 ID。
     */
    private Long id;
    /**
     * 触发动作的用户 ID，例如点赞人、评论人、关注人。
     */
    private Long actorId;
    /**
     * 类型字段，用于区分通知类型、消息类型等。
     */
    private String type;
    /**
     * 通知或关联对象的目标类型，例如 POST、COMMENT、THREAD。
     */
    private String targetType;
    /**
     * 目标对象 ID，例如帖子 ID、评论 ID、会话 ID。
     */
    private Long targetId;
    /**
     * 标题文本，用于帖子、通知等场景。
     */
    private String title;
    /**
     * 正文内容，帖子、评论、消息等核心文本都通过该字段承载。
     */
    private String content;
    /**
     * 前端友好的已读布尔值。
     */
    private Boolean read;
    /**
     * 已读时间。
     */
    private LocalDateTime readAt;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 通知触发者的概要信息。
     */
    private UserSummaryVo actor;
}
