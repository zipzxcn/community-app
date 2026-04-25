package com.community.backend.service;

import com.community.backend.common.api.PageResponse;
import com.community.backend.vo.follow.FollowUserVo;

public interface FollowService {

    void follow(Long currentUserId, Long targetUserId);

    void unfollow(Long currentUserId, Long targetUserId);

    PageResponse<FollowUserVo> listFollowing(Long currentUserId, Long page, Long size);

    PageResponse<FollowUserVo> listFollowers(Long currentUserId, Long page, Long size);
}
