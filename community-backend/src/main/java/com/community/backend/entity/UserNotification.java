package com.community.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_notification")
/**
 * 用户通知实体。
 */
public class UserNotification {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 接收方用户 ID。
     */
    private Long receiverId;
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
     * 扩展 JSON 字段，预留更多结构化元数据。
     */
    private String extraJson;
    /**
     * 是否已读，0/1 存储以兼容数据库统计与条件查询。
     */
    private Integer isRead;
    /**
     * 已读时间。
     */
    private LocalDateTime readAt;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
