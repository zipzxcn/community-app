package com.community.backend.vo.user;

import lombok.Data;

@Data
/**
 * 用户主页信息返回体。
 */
public class UserProfileVo {

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
    /**
     * 个人简介，用于用户主页展示。
     */
    private String bio;
    /**
     * 用户已发布帖子数或标签关联帖子数等统计值。
     */
    private Integer postCount;
    /**
     * 粉丝数量统计。
     */
    private Integer followerCount;
    /**
     * 关注数量统计。
     */
    private Integer followingCount;
    /**
     * 当前登录用户是否已关注该主页用户。
     */
    private Boolean followed;
    /**
     * 双方是否处于互相关注状态。
     */
    private Boolean mutualFollow;
}
