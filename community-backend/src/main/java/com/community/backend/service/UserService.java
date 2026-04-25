package com.community.backend.service;

import com.community.backend.common.api.PageResponse;
import com.community.backend.dto.user.UpdateProfileRequest;
import com.community.backend.dto.user.UserSearchQuery;
import com.community.backend.vo.post.PostListItemVo;
import com.community.backend.vo.user.UserProfileVo;
import com.community.backend.vo.user.UserSummaryVo;

public interface UserService {

    UserProfileVo getProfile(Long targetUserId, Long currentUserId);

    PageResponse<UserSummaryVo> search(UserSearchQuery query);

    UserProfileVo updateProfile(Long currentUserId, UpdateProfileRequest request);

    PageResponse<PostListItemVo> listUserPosts(Long userId, Long currentUserId, Long page, Long size);

    PageResponse<PostListItemVo> listUserFavorites(Long userId, Long currentUserId, Long page, Long size);

    PageResponse<PostListItemVo> listUserLikes(Long userId, Long currentUserId, Long page, Long size);
}
