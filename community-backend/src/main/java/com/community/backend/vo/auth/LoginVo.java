package com.community.backend.vo.auth;

import lombok.Data;

@Data
/**
 * 登录成功返回体：token 信息 + 当前用户信息。
 */
public class LoginVo {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private CurrentUserVo userInfo;
}
