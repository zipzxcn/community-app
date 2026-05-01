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
@TableName("chat_message")
/**
 * 聊天消息实体。
 */
public class ChatMessage {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 聊天会话 ID，一对一聊天双方共用同一个 thread。
     */
    private Long threadId;
    /**
     * 消息发送方用户 ID。
     */
    private Long senderId;
    /**
     * 接收方用户 ID。
     */
    private Long receiverId;
    /**
     * 客户端生成的消息唯一标识，用于前后端幂等和去重。
     */
    private String clientMsgId;
    /**
     * 消息类型，当前主要支持 TEXT 和 IMAGE。
     */
    private String messageType;
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
     * 业务状态字段，不同对象有各自的枚举语义，例如 ACTIVE、PUBLISHED、HIDDEN。
     */
    private String status;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
}
