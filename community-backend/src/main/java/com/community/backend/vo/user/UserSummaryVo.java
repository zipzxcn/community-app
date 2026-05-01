package com.community.backend.vo.user;

import lombok.Data;

@Data
/**
 * 用户摘要信息返回体。
 */
public class UserSummaryVo {

    /**
     * 用户 ID。
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
}
