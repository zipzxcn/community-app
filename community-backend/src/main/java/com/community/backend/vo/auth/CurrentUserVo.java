package com.community.backend.vo.auth;

import lombok.Data;

@Data
/**
 * 当前登录用户信息返回体。
 */
public class CurrentUserVo {

    /**
     * 主键 ID。
     */
    private Long id;
    /**
     * 登录用户名/账号标识，系统内要求唯一。
     */
    private String username;
    /**
     * 用户昵称，用于前台展示。
     */
    private String nickname;
    /**
     * 头像访问地址，通常指向对象存储公开代理地址。
     */
    private String avatarUrl;
    /**
     * 个人简介，用于用户主页展示。
     */
    private String bio;
}
