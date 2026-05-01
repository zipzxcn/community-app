package com.community.backend.vo.chat;

import com.community.backend.vo.user.UserSummaryVo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 会话列表/会话详情返回体。
 */
public class ChatThreadVo {

    /**
     * 会话 ID，前端据此拉取消息记录并发送消息。
     */
    private Long threadId;
    /**
     * 业务状态字段，不同对象有各自的枚举语义，例如 ACTIVE、PUBLISHED、HIDDEN。
     */
    private String status;
    /**
     * 会话列表展示的最后一条消息摘要。
     */
    private String lastMessagePreview;
    /**
     * 最后一条消息发送时间，用于会话排序。
     */
    private LocalDateTime lastMessageAt;
    /**
     * 未读数量，用于红点提醒与会话列表展示。
     */
    private Integer unreadCount;
    /**
     * 当前会话的对端用户信息。
     */
    private UserSummaryVo peerUser;
}
