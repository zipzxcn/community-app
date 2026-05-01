package com.community.backend.controller;

import com.community.backend.common.api.ApiResponse;
import com.community.backend.dto.auth.LoginRequest;
import com.community.backend.dto.auth.RefreshTokenRequest;
import com.community.backend.dto.auth.RegisterRequest;
import com.community.backend.security.SecurityUtils;
import com.community.backend.service.AuthService;
import com.community.backend.vo.auth.AuthCaptchaVo;
import com.community.backend.vo.auth.CurrentUserVo;
import com.community.backend.vo.auth.LoginVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证模块控制器：负责注册、登录、令牌续期与当前登录态查询。
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/captcha")
    public ApiResponse<AuthCaptchaVo> captcha() {
        return ApiResponse.success(authService.captcha());
    }

    /**
     * 用户注册：创建基础账号信息，返回新用户 ID。
     */
    @PostMapping("/register")
    public ApiResponse<Map<String, Long>> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return ApiResponse.success(Map.of("userId", userId));
    }

    /**
     * 用户登录：校验账号密码并签发 access/refresh token。
     */
    @PostMapping("/login")
    public ApiResponse<LoginVo> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    /**
     * 获取当前登录用户信息：由 JWT 解析出的 userId 决定。
     */
    @GetMapping("/me")
    public ApiResponse<CurrentUserVo> me() {
        Long userId = SecurityUtils.requireUserId();
        return ApiResponse.success(authService.me(userId));
    }

    /**
     * 刷新令牌：用 refreshToken 换取新的 accessToken 与 refreshToken。
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginVo> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    /**
     * 登出：撤销指定 refreshToken 对应会话，保证后续不可继续刷新。
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        Long userId = SecurityUtils.requireUserId();
        authService.logout(userId, request);
        return ApiResponse.success(null);
    }
}
