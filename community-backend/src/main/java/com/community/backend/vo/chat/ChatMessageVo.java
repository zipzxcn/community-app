package com.community.backend.vo.chat;

import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 聊天消息返回体。
 */
public class ChatMessageVo {

    private Long id;
    private Long threadId;
    private Long senderId;
    private Long receiverId;
    private String clientMsgId;
    private String messageType;
    private String content;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
