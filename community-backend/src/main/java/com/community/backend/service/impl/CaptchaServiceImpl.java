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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    private static final char[] CAPTCHA_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    private final ConcurrentHashMap<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Value("${app.auth.captcha.expire-seconds:300}")
    private long expireSeconds;

    @Override
    public AuthCaptchaVo create() {
        purgeExpired();
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String code = generateCode(4);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expireSeconds);
        captchaStore.put(captchaId, new CaptchaEntry(code, expiresAt));

        AuthCaptchaVo vo = new AuthCaptchaVo();
        vo.setCaptchaId(captchaId);
        vo.setCaptchaSvg(buildSvg(code));
        vo.setExpireInSeconds(expireSeconds);
        return vo;
    }

    @Override
    public void validate(String captchaId, String captchaCode) {
        purgeExpired();
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            throw BizException.of(ErrorCode.AUTH_CAPTCHA_INVALID);
        }
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

    private String buildSvg(String code) {
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
