package com.community.backend.vo.user;

import lombok.Data;

@Data
public class UserRecommendVo {

    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private Integer postCount;
    private Integer followerCount;
    private Boolean followed;
    private Boolean mutualFollow;
    private String recommendReason;
}
