package com.community.backend.service;

import com.community.backend.common.api.PageResponse;
import com.community.backend.dto.chat.MarkReadRequest;
import com.community.backend.dto.chat.SendMessageRequest;
import com.community.backend.vo.chat.ChatMessageVo;
import com.community.backend.vo.chat.ChatThreadVo;

public interface ChatService {

    PageResponse<ChatThreadVo> listThreads(Long currentUserId, Long page, Long size);

    ChatThreadVo openThread(Long currentUserId, Long targetUserId);

    ChatThreadVo getThread(Long currentUserId, Long threadId);

    PageResponse<ChatMessageVo> listMessages(Long currentUserId, Long threadId, Long cursor, Long size);

    ChatMessageVo sendMessage(Long currentUserId, Long threadId, SendMessageRequest request);

    void markRead(Long currentUserId, Long threadId, MarkReadRequest request);
}
