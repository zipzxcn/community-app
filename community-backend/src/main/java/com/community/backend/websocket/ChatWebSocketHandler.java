package com.community.backend.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 聊天实时通道处理器：当前仅负责连接管理和轻量事件响应。
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatWebSocketSessionManager sessionManager;
    private final ChatPushService chatPushService;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ChatWebSocketSessionManager sessionManager,
                                ChatPushService chatPushService,
                                ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.chatPushService = chatPushService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            return;
        }
        // 建立连接后先登记 session，后续私信新消息与已读事件就能精准推送到该用户。
        sessionManager.register(userId, session);
        chatPushService.sendToSession(session, "chat.connected", Map.of(
                "userId", userId,
                "serverTime", LocalDateTime.now()
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode payload = objectMapper.readTree(message.getPayload());
            String type = payload.path("type").asText("");
            // 当前 WebSocket 只做轻量实时推送与心跳保活，真正的消息收发仍走 REST，降低复杂度。
            if ("chat.ping".equals(type)) {
                chatPushService.sendToSession(session, "chat.pong", Map.of("serverTime", LocalDateTime.now()));
                return;
            }
            chatPushService.sendToSession(session, "chat.error", Map.of("message", "暂不支持该实时指令，请继续使用 REST 接口"));
        } catch (Exception ex) {
            chatPushService.sendToSession(session, "chat.error", Map.of("message", "消息格式错误"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        unregister(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        unregister(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private void unregister(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId != null) {
            sessionManager.unregister(userId, session);
        }
    }

    private Long getUserId(WebSocketSession session) {
        Object value = session.getAttributes().get("userId");
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            return Long.parseLong(stringValue);
        }
        return null;
    }
}
