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

    /**
     * 推送给某个用户的全部在线连接。
     * 解决的问题：同一账号可能同时在 PC 和移动端在线，需要所有端都收到实时事件。
     */
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
            // WebSocketSession 发送不是天然线程安全的，这里串行化单个 session 的发送动作。
            synchronized (session) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            }
        } catch (Exception ignored) {
            // 推送失败不影响主业务，前端会继续保留 REST 刷新兜底。
        }
    }
}
