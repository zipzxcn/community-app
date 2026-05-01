package com.community.backend.vo.follow;

import lombok.Data;

@Data
/**
 * 关注关系用户项返回体。
 */
public class FollowUserVo {

    /**
     * 目标用户 ID。
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
     * 当前登录用户是否已关注该用户。
     */
    private Boolean followedByMe;
    /**
     * 双方是否处于互相关注状态。
     */
    private Boolean mutualFollow;
}
