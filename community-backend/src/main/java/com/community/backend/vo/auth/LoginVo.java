package com.community.backend.vo.auth;

import lombok.Data;

@Data
/**
 * 登录成功返回体：token 信息 + 当前用户信息。
 */
public class LoginVo {

    /**
     * JWT 访问令牌，请求受保护接口时通过 Authorization: Bearer 发送。
     */
    private String accessToken;
    /**
     * 用于续签 accessToken 的长效令牌原文，只在客户端保存。
     */
    private String refreshToken;
    /**
     * accessToken 的有效期秒数。
     */
    private Long expiresIn;
    /**
     * 登录成功后返回的当前用户信息，前端可直接写入 Pinia。
     */
    private CurrentUserVo userInfo;
}
