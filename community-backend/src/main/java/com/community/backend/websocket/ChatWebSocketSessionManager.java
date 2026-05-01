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

    /**
     * 注册用户在线会话。
     * 使用 ConcurrentHashMap + KeySetView 支持并发写入，适合多请求同时建立连接的场景。
     */
    public void register(Long userId, WebSocketSession session) {
        sessionsByUser.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    /**
     * 注销单个连接；如果该用户已无任何在线连接，则连用户键一并移除。
     */
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
