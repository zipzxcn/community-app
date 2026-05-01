package com.community.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
/**
 * 刷新/登出请求参数：携带 refreshToken。
 */
public class RefreshTokenRequest {

    /**
     * 用于续签 accessToken 的长效令牌原文，只在客户端保存。
     */
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
