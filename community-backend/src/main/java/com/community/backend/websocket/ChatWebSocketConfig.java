package com.community.backend.websocket;

import com.community.backend.config.CorsProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 注册聊天 WebSocket 入口。
 */
@Configuration
@EnableWebSocket
public class ChatWebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final ChatHandshakeInterceptor chatHandshakeInterceptor;
    private final CorsProperties corsProperties;

    public ChatWebSocketConfig(ChatWebSocketHandler chatWebSocketHandler,
                               ChatHandshakeInterceptor chatHandshakeInterceptor,
                               CorsProperties corsProperties) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.chatHandshakeInterceptor = chatHandshakeInterceptor;
        this.corsProperties = corsProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // WebSocket 入口固定为 /ws/chat，前端会基于当前 accessToken 拼接 token 参数发起连接。
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(chatHandshakeInterceptor)
                .setAllowedOriginPatterns(corsProperties.getAllowedOrigins().toArray(String[]::new));
    }
}
