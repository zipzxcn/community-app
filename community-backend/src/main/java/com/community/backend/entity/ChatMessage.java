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

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long threadId;
    private Long senderId;
    private Long receiverId;
    private String clientMsgId;
    private String messageType;
    private String content;
    private String extraJson;
    private Integer isRead;
    private LocalDateTime readAt;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
