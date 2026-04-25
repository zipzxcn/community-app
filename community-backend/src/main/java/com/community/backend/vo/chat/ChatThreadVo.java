package com.community.backend.vo.chat;

import com.community.backend.vo.user.UserSummaryVo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 会话列表/会话详情返回体。
 */
public class ChatThreadVo {

    private Long threadId;
    private String status;
    private String lastMessagePreview;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
    private UserSummaryVo peerUser;
}
