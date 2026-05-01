package com.community.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类：负责签发、解析与校验 access token。
 */
@Component
public class JwtTokenProvider {

    /**
     * JWT 原始签名密钥字符串。
     */
    @Value("${app.security.jwt.secret:change-this-secret-change-this-secret}")
    private String secret;

    /**
     * accessToken 过期秒数。
     */
    @Value("${app.security.jwt.access-token-expire-seconds:7200}")
    private long accessTokenExpireSeconds;

    /**
     * JJWT 解析与签发时真正使用的 SecretKey 对象。
     */
    private SecretKey secretKey;

    /**
     * 启动时初始化签名密钥。
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 access token。
     */
    public String createAccessToken(Long userId, String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(Map.of("uid", userId, "username", username))
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenExpireSeconds)))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 token claims；解析异常由上层统一处理为无效令牌。
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取用户名（subject）。
     */
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 获取用户 ID，兼容 Integer/Long/String 三种历史类型。
     */
    public Long getUserId(String token) {
        Object uid = parseClaims(token).get("uid");
        if (uid instanceof Integer value) {
            return value.longValue();
        }
        if (uid instanceof Long value) {
            return value;
        }
        if (uid instanceof String value) {
            return Long.parseLong(value);
        }
        return null;
    }

    /**
     * 校验 token 是否可用（签名/过期等）。
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * access token 有效期秒数，用于回传前端倒计时。
     */
    public long getAccessTokenExpireSeconds() {
        return accessTokenExpireSeconds;
    }
}
