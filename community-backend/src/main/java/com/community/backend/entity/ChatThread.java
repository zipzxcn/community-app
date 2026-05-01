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
@TableName("chat_thread")
/**
 * 一对一会话实体。
 */
public class ChatThread {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 会话中排序靠前的一方用户 ID，用于确保一对一会话唯一。
     */
    private Long userAId;
    /**
     * 会话中排序靠后的另一方用户 ID。
     */
    private Long userBId;
    /**
     * 业务状态字段，不同对象有各自的枚举语义，例如 ACTIVE、PUBLISHED、HIDDEN。
     */
    private String status;
    /**
     * 会话最后一条消息 ID，用于列表摘要和已读推进。
     */
    private Long lastMessageId;
    /**
     * 会话列表展示的最后一条消息摘要。
     */
    private String lastMessagePreview;
    /**
     * 最后一条消息发送时间，用于会话排序。
     */
    private LocalDateTime lastMessageAt;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
}
