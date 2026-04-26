package com.community.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 将聊天事件推送给指定用户的全部在线会话。
 */
@Component
public class ChatPushService {

    private final ChatWebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public ChatPushService(ChatWebSocketSessionManager sessionManager, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    public void pushToUser(Long userId, String type, Object data) {
        for (WebSocketSession session : sessionManager.getSessions(userId)) {
            sendToSession(session, type, data);
        }
    }

    public void sendToSession(WebSocketSession session, String type, Object data) {
        if (!session.isOpen()) {
            return;
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", type);
            payload.put("data", data);
            synchronized (session) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            }
        } catch (Exception ignored) {
            // 推送失败不影响主业务，前端会继续保留 REST 刷新兜底。
        }
    }
}
