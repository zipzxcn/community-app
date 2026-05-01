package com.community.backend.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Token 哈希工具：
 * 1) 对 refreshToken 做单向摘要后再落库，避免数据库泄漏时直接暴露令牌原文。
 * 2) 这里使用 JDK 自带 MessageDigest，所属依赖来自 JDK 标准库，无需额外 Maven 依赖。
 */
public final class TokenHashUtils {

    private TokenHashUtils() {
    }

    /**
     * 对任意字符串执行 SHA-256 摘要。
     * 典型场景：登录后生成 refreshToken 原文发给前端，但数据库里仅保存哈希值。
     */
    public static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }
    }
}
