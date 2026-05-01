package com.community.backend.service.impl;

import com.community.backend.common.error.BizException;
import com.community.backend.common.error.ErrorCode;
import com.community.backend.service.CaptchaService;
import com.community.backend.vo.auth.AuthCaptchaVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图形验证码服务：
 * 1) 当前实现使用内存 ConcurrentHashMap 保存验证码，不依赖 Redis。
 * 2) 适合单机开发与 MVP 场景，若未来多实例部署，建议迁移到 Redis 等共享存储。
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final char[] CAPTCHA_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    private final ConcurrentHashMap<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Value("${app.auth.captcha.expire-seconds:300}")
    private long expireSeconds;

    @Override
    public AuthCaptchaVo create() {
        // 先清理过期验证码，避免内存持续堆积。
        purgeExpired();
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String code = generateCode(4);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expireSeconds);
        // 以 captchaId 作为一次性票据，前端提交登录/注册时必须原样带回。
        captchaStore.put(captchaId, new CaptchaEntry(code, expiresAt));

        AuthCaptchaVo vo = new AuthCaptchaVo();
        vo.setCaptchaId(captchaId);
        vo.setCaptchaImageBase64(buildSvgBase64(code));
        vo.setExpireInSeconds(expireSeconds);
        return vo;
    }

    @Override
    public void validate(String captchaId, String captchaCode) {
        purgeExpired();
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw BizException.of(ErrorCode.AUTH_CAPTCHA_INVALID);
        }
        // 验证成功或失败后都移除，保证验证码只能使用一次。
        CaptchaEntry entry = captchaStore.remove(captchaId);
        if (entry == null || entry.expiresAt().isBefore(LocalDateTime.now())) {
            throw BizException.of(ErrorCode.AUTH_CAPTCHA_EXPIRED);
        }
        if (!entry.code().equalsIgnoreCase(captchaCode.trim())) {
            throw BizException.of(ErrorCode.AUTH_CAPTCHA_INVALID);
        }
    }

    private void purgeExpired() {
        LocalDateTime now = LocalDateTime.now();
        captchaStore.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private String generateCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(CAPTCHA_CHARS[random.nextInt(CAPTCHA_CHARS.length)]);
        }
        return builder.toString();
    }

    private String buildSvgBase64(String code) {
        String svg = buildSvg(code);
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String buildSvg(String code) {
        // 直接拼 SVG 可避免额外图片库依赖，浏览器也能天然展示。
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns='http://www.w3.org/2000/svg' width='132' height='44' viewBox='0 0 132 44'>");
        svg.append("<rect width='132' height='44' rx='10' fill='#f8fafc'/>");
        for (int i = 0; i < 6; i++) {
            int x1 = random.nextInt(132);
            int y1 = random.nextInt(44);
            int x2 = random.nextInt(132);
            int y2 = random.nextInt(44);
            svg.append("<line x1='").append(x1).append("' y1='").append(y1)
                    .append("' x2='").append(x2).append("' y2='").append(y2)
                    .append("' stroke='#cbd5e1' stroke-width='1'/>");
        }
        for (int i = 0; i < code.length(); i++) {
            int x = 18 + i * 24;
            int y = 29 + random.nextInt(6);
            int rotate = random.nextInt(21) - 10;
            String color = i % 2 == 0 ? "#0f766e" : "#2563eb";
            svg.append("<text x='").append(x)
                    .append("' y='").append(y)
                    .append("' transform='rotate(").append(rotate).append(' ').append(x).append(' ').append(y).append(")'")
                    .append(" font-size='24' font-family='Arial, sans-serif' font-weight='700' fill='").append(color).append("'>")
                    .append(code.charAt(i))
                    .append("</text>");
        }
        svg.append("</svg>");
        return svg.toString();
    }

    private record CaptchaEntry(String code, LocalDateTime expiresAt) {
    }
}
