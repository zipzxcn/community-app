package com.community.backend.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Auth 模块集成测试：
 * 注册 -> 登录 -> me -> refresh -> logout -> refresh 失效。
 */
class AuthApiIT extends SupportApiTest {

    @Test
    void shouldCompleteAuthLifecycle() throws Exception {
        Session session = createAndLogin("it_auth");

        JsonNode me = getJson("/api/v1/auth/me", session.accessToken());
        assertOk(me);
        Assertions.assertEquals(session.userId().longValue(), me.path("data").path("id").asLong());

        JsonNode refresh = postJson("/api/v1/auth/refresh",
                Map.of("refreshToken", session.refreshToken()),
                null);
        assertOk(refresh);
        String refreshedAccessToken = refresh.path("data").path("accessToken").asText();
        String refreshedRefreshToken = refresh.path("data").path("refreshToken").asText();
        Assertions.assertFalse(refreshedAccessToken.isBlank());
        Assertions.assertFalse(refreshedRefreshToken.isBlank());

        JsonNode logout = postJson("/api/v1/auth/logout",
                Map.of("refreshToken", refreshedRefreshToken),
                refreshedAccessToken);
        assertOk(logout);

        // 登出后同一个 refreshToken 应被撤销，刷新应失败（20014）
        JsonNode refreshAfterLogout = postJson("/api/v1/auth/refresh",
                Map.of("refreshToken", refreshedRefreshToken),
                null);
        Assertions.assertEquals(20014, refreshAfterLogout.path("code").asInt());
    }

    @Test
    void shouldRejectInvalidCaptchaWhenLogin() throws Exception {
        String suffix = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String username = "it_captcha_" + suffix;
        String password = "12345678";
        String nickname = "IT_" + suffix;

        CaptchaData registerCaptcha = fetchCaptcha();
        JsonNode register = postJson("/api/v1/auth/register",
                Map.of(
                        "username", username,
                        "password", password,
                        "nickname", nickname,
                        "captchaId", registerCaptcha.captchaId(),
                        "captchaCode", registerCaptcha.captchaCode()
                ),
                null);
        assertOk(register);

        CaptchaData loginCaptcha = fetchCaptcha();
        JsonNode login = postJson("/api/v1/auth/login",
                Map.of(
                        "username", username,
                        "password", password,
                        "captchaId", loginCaptcha.captchaId(),
                        "captchaCode", "WRNG"
                ),
                null);
        Assertions.assertEquals(20016, login.path("code").asInt());
    }
}
