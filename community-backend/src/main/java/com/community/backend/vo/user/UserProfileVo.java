package com.community.backend.vo.user;

import lombok.Data;

@Data
/**
 * 用户主页信息返回体。
 */
public class UserProfileVo {

    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private Integer postCount;
    private Integer followerCount;
    private Integer followingCount;
    private Boolean followed;
    private Boolean mutualFollow;
}
