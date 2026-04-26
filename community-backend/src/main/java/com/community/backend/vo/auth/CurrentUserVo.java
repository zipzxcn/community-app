package com.community.backend.vo.auth;

import lombok.Data;

@Data
/**
 * 当前登录用户信息返回体。
 */
public class CurrentUserVo {

    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String bio;
}
