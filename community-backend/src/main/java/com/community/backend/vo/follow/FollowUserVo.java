package com.community.backend.vo.follow;

import lombok.Data;

@Data
/**
 * 关注关系用户项返回体。
 */
public class FollowUserVo {

    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private Boolean followedByMe;
    private Boolean mutualFollow;
}
