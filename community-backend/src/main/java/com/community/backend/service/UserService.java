package com.community.backend.service;

import com.community.backend.common.api.PageResponse;
import com.community.backend.dto.user.UpdateProfileRequest;
import com.community.backend.dto.user.UserSearchQuery;
import com.community.backend.vo.user.UserProfileVo;
import com.community.backend.vo.user.UserSummaryVo;

public interface UserService {

    UserProfileVo getProfile(Long targetUserId, Long currentUserId);

    PageResponse<UserSummaryVo> search(UserSearchQuery query);

    UserProfileVo updateProfile(Long currentUserId, UpdateProfileRequest request);
}
