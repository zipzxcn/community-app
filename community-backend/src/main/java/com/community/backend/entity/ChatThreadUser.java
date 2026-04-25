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
@TableName("chat_thread_user")
/**
 * 会话-用户状态实体：维护未读计数与已读游标。
 */
public class ChatThreadUser {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long threadId;
    private Long userId;
    private Integer unreadCount;
    private Long lastReadMessageId;
    private LocalDateTime lastReadAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
