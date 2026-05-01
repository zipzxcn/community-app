package com.community.backend.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 聊天消息返回体。
 */
public class ChatMessageVo {

    /**
     * 消息 ID。
     */
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
}
