package com.community.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.backend.common.api.PageResponse;
import com.community.backend.common.error.BizException;
import com.community.backend.common.error.ErrorCode;
import com.community.backend.dto.user.UpdateProfileRequest;
import com.community.backend.dto.user.UserSearchQuery;
import com.community.backend.entity.AppUser;
import com.community.backend.entity.UserFollow;
import com.community.backend.mapper.AppUserMapper;
import com.community.backend.mapper.UserFollowMapper;
import com.community.backend.service.UserService;
import com.community.backend.vo.user.UserProfileVo;
import com.community.backend.vo.user.UserSummaryVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户服务实现：用户主页、搜索与资料编辑。
 */
@Service
public class UserServiceImpl implements UserService {

    private final AppUserMapper appUserMapper;
    private final UserFollowMapper userFollowMapper;

    public UserServiceImpl(AppUserMapper appUserMapper, UserFollowMapper userFollowMapper) {
        this.appUserMapper = appUserMapper;
        this.userFollowMapper = userFollowMapper;
    }

    /**
     * 用户主页：
     * - 基础资料
     * - 登录态下补充关注/互关状态
     */
    @Override
    public UserProfileVo getProfile(Long targetUserId, Long currentUserId) {
        AppUser target = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getId, targetUserId)
                .eq(AppUser::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (target == null) {
            throw BizException.of(ErrorCode.USER_NOT_FOUND);
        }

        UserProfileVo vo = new UserProfileVo();
        vo.setId(target.getId());
        vo.setUsername(target.getUsername());
        vo.setNickname(target.getNickname());
        vo.setAvatarUrl(target.getAvatarUrl());
        vo.setBio(target.getBio());
        vo.setPostCount(target.getPostCount());
        vo.setFollowerCount(target.getFollowerCount());
        vo.setFollowingCount(target.getFollowingCount());
        vo.setFollowed(Boolean.FALSE);
        vo.setMutualFollow(Boolean.FALSE);

        if (currentUserId != null && !currentUserId.equals(targetUserId)) {
            boolean followed = isActiveFollow(currentUserId, targetUserId);
            vo.setFollowed(followed);
            if (followed) {
                vo.setMutualFollow(isActiveFollow(targetUserId, currentUserId));
            }
        }
        return vo;
    }

    /**
     * 用户搜索：按用户名/昵称关键词分页检索活跃用户。
     */
    @Override
    public PageResponse<UserSummaryVo> search(UserSearchQuery query) {
        Page<AppUser> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getIsDeleted, 0)
                .eq(AppUser::getStatus, "ACTIVE")
                .orderByDesc(AppUser::getCreatedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(AppUser::getUsername, query.getKeyword())
                    .or()
                    .like(AppUser::getNickname, query.getKeyword()));
        }

        Page<AppUser> result = appUserMapper.selectPage(page, wrapper);
        List<UserSummaryVo> list = result.getRecords().stream().map(this::toSummaryVo).toList();

        return PageResponse.<UserSummaryVo>builder()
                .list(list)
                .page(result.getCurrent())
                .size(result.getSize())
                .total(result.getTotal())
                .hasMore(result.getCurrent() * result.getSize() < result.getTotal())
                .build();
    }

    /**
     * 更新当前用户资料，仅更新请求中非空字段。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileVo updateProfile(Long currentUserId, UpdateProfileRequest request) {
        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, currentUserId)
                .eq(AppUser::getIsDeleted, 0)
                .set(request.getNickname() != null, AppUser::getNickname, request.getNickname())
                .set(request.getAvatarUrl() != null, AppUser::getAvatarUrl, request.getAvatarUrl())
                .set(request.getBio() != null, AppUser::getBio, request.getBio()));
        return getProfile(currentUserId, currentUserId);
    }

    /**
     * 判断是否为有效关注关系。
     */
    private boolean isActiveFollow(Long followerId, Long followeeId) {
        return userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, followerId)
                .eq(UserFollow::getFolloweeId, followeeId)
                .eq(UserFollow::getStatus, "ACTIVE")) > 0;
    }

    /**
     * 用户实体转摘要信息 VO。
     */
    private UserSummaryVo toSummaryVo(AppUser user) {
        UserSummaryVo vo = new UserSummaryVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        return vo;
    }
}
