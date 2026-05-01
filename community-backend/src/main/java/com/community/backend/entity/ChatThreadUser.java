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
     * 当前业务记录所属的用户 ID。
     */
    private Long userId;
    /**
     * 未读数量，用于红点提醒与会话列表展示。
     */
    private Integer unreadCount;
    /**
     * 最后一条已读消息 ID，用于按游标推进已读状态。
     */
    private Long lastReadMessageId;
    /**
     * 最近一次标记已读的时间。
     */
    private LocalDateTime lastReadAt;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
}
