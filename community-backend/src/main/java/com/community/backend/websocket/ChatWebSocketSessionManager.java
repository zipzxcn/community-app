package com.community.backend.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理用户与 WebSocket 会话的映射，支持单用户多端同时在线。
 */
@Component
public class ChatWebSocketSessionManager {

    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        sessionsByUser.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(Long userId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByUser.remove(userId);
        }
    }

    public Set<WebSocketSession> getSessions(Long userId) {
        return sessionsByUser.getOrDefault(userId, Collections.emptySet());
    }
}
