package com.community.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.backend.common.api.PageResponse;
import com.community.backend.common.constant.NotificationTargetType;
import com.community.backend.common.constant.NotificationType;
import com.community.backend.common.error.BizException;
import com.community.backend.common.error.ErrorCode;
import com.community.backend.entity.AppUser;
import com.community.backend.entity.UserFollow;
import com.community.backend.mapper.AppUserMapper;
import com.community.backend.mapper.UserFollowMapper;
import com.community.backend.service.FollowService;
import com.community.backend.service.NotificationService;
import com.community.backend.vo.follow.FollowUserVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 关注服务实现：关注关系维护、统计字段维护与通知触发。
 */
@Service
public class FollowServiceImpl implements FollowService {

    private final UserFollowMapper userFollowMapper;
    private final AppUserMapper appUserMapper;
    private final NotificationService notificationService;

    public FollowServiceImpl(UserFollowMapper userFollowMapper,
                             AppUserMapper appUserMapper,
                             NotificationService notificationService) {
        this.userFollowMapper = userFollowMapper;
        this.appUserMapper = appUserMapper;
        this.notificationService = notificationService;
    }

    /**
     * 关注用户：
     * - 新增关系或恢复历史关系
     * - 维护双方关注统计
     * - 触发关注通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw BizException.of(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }
        mustGetActiveUser(targetUserId);

        UserFollow existing = userFollowMapper.selectOne(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, currentUserId)
                .eq(UserFollow::getFolloweeId, targetUserId)
                .last("LIMIT 1"));

        if (existing == null) {
            userFollowMapper.insert(UserFollow.builder()
                    .followerId(currentUserId)
                    .followeeId(targetUserId)
                    .status("ACTIVE")
                    .followedAt(LocalDateTime.now())
                    .build());
            increaseFollowCount(currentUserId, targetUserId);
            // 关注成功后给被关注方发送通知
            createFollowNotification(currentUserId, targetUserId);
            return;
        }
        if ("ACTIVE".equals(existing.getStatus())) {
            return;
        }
        userFollowMapper.update(null, new LambdaUpdateWrapper<UserFollow>()
                .eq(UserFollow::getId, existing.getId())
                .set(UserFollow::getStatus, "ACTIVE")
                .set(UserFollow::getFollowedAt, LocalDateTime.now())
                .set(UserFollow::getUnfollowedAt, null));
        increaseFollowCount(currentUserId, targetUserId);
        // 重新关注也需要通知被关注方
        createFollowNotification(currentUserId, targetUserId);
    }

    /**
     * 取消关注：仅将状态改为 INACTIVE，并回收统计数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            return;
        }
        UserFollow existing = userFollowMapper.selectOne(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, currentUserId)
                .eq(UserFollow::getFolloweeId, targetUserId)
                .last("LIMIT 1"));
        if (existing == null || !"ACTIVE".equals(existing.getStatus())) {
            return;
        }
        userFollowMapper.update(null, new LambdaUpdateWrapper<UserFollow>()
                .eq(UserFollow::getId, existing.getId())
                .set(UserFollow::getStatus, "INACTIVE")
                .set(UserFollow::getUnfollowedAt, LocalDateTime.now()));
        decreaseFollowCount(currentUserId, targetUserId);
    }

    /**
     * 查询我关注的人列表，附带互关信息。
     */
    @Override
    public PageResponse<FollowUserVo> listFollowing(Long currentUserId, Long page, Long size) {
        Page<UserFollow> pager = new Page<>(page, size);
        Page<UserFollow> result = userFollowMapper.selectPage(pager, new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, currentUserId)
                .eq(UserFollow::getStatus, "ACTIVE")
                .orderByDesc(UserFollow::getUpdatedAt));

        List<Long> userIds = result.getRecords().stream().map(UserFollow::getFolloweeId).toList();
        if (userIds.isEmpty()) {
            return emptyPage(result);
        }
        Map<Long, AppUser> userMap = loadUserMap(userIds);
        Set<Long> mutualSet = loadMutualSet(currentUserId, userIds);

        List<FollowUserVo> list = userIds.stream()
                .map(userMap::get)
                .filter(user -> user != null)
                .map(user -> {
                    FollowUserVo vo = toVo(user);
                    vo.setFollowedByMe(Boolean.TRUE);
                    vo.setMutualFollow(mutualSet.contains(user.getId()));
                    return vo;
                })
                .toList();

        return toPageResponse(result, list);
    }

    /**
     * 查询我的粉丝列表，附带我是否已回关。
     */
    @Override
    public PageResponse<FollowUserVo> listFollowers(Long currentUserId, Long page, Long size) {
        Page<UserFollow> pager = new Page<>(page, size);
        Page<UserFollow> result = userFollowMapper.selectPage(pager, new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFolloweeId, currentUserId)
                .eq(UserFollow::getStatus, "ACTIVE")
                .orderByDesc(UserFollow::getUpdatedAt));

        List<Long> followerIds = result.getRecords().stream().map(UserFollow::getFollowerId).toList();
        if (followerIds.isEmpty()) {
            return emptyPage(result);
        }
        Map<Long, AppUser> userMap = loadUserMap(followerIds);
        Set<Long> followedByMeSet = loadFollowedByMeSet(currentUserId, followerIds);

        List<FollowUserVo> list = followerIds.stream()
                .map(userMap::get)
                .filter(user -> user != null)
                .map(user -> {
                    FollowUserVo vo = toVo(user);
                    boolean followedByMe = followedByMeSet.contains(user.getId());
                    vo.setFollowedByMe(followedByMe);
                    vo.setMutualFollow(followedByMe);
                    return vo;
                })
                .toList();

        return toPageResponse(result, list);
    }

    /**
     * 查询我的互关列表（我关注了 ta，ta 也关注了我）。
     */
    @Override
    public PageResponse<FollowUserVo> listMutual(Long currentUserId, Long page, Long size) {
        List<UserFollow> followingAll = userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, currentUserId)
                .eq(UserFollow::getStatus, "ACTIVE")
                .orderByDesc(UserFollow::getUpdatedAt));
        if (followingAll.isEmpty()) {
            return PageResponse.<FollowUserVo>builder()
                    .list(Collections.emptyList())
                    .page(page)
                    .size(size)
                    .total(0L)
                    .hasMore(Boolean.FALSE)
                    .build();
        }

        List<Long> followingIds = followingAll.stream().map(UserFollow::getFolloweeId).toList();
        Set<Long> mutualSet = loadMutualSet(currentUserId, followingIds);
        List<Long> mutualOrderedIds = followingAll.stream()
                .map(UserFollow::getFolloweeId)
                .filter(mutualSet::contains)
                .toList();
        long total = mutualOrderedIds.size();
        if (total == 0) {
            return PageResponse.<FollowUserVo>builder()
                    .list(Collections.emptyList())
                    .page(page)
                    .size(size)
                    .total(0L)
                    .hasMore(Boolean.FALSE)
                    .build();
        }

        // 基于互关有序列表做分页，确保返回顺序稳定且不受 SQL 方言影响。
        int from = (int) Math.max((page - 1) * size, 0);
        if (from >= mutualOrderedIds.size()) {
            return PageResponse.<FollowUserVo>builder()
                    .list(Collections.emptyList())
                    .page(page)
                    .size(size)
                    .total(total)
                    .hasMore(Boolean.FALSE)
                    .build();
        }
        int to = (int) Math.min(from + size, mutualOrderedIds.size());
        List<Long> pageIds = mutualOrderedIds.subList(from, to);
        Map<Long, AppUser> userMap = loadUserMap(pageIds);

        List<FollowUserVo> list = pageIds.stream()
                .map(userMap::get)
                .filter(user -> user != null)
                .map(user -> {
                    FollowUserVo vo = toVo(user);
                    vo.setFollowedByMe(Boolean.TRUE);
                    vo.setMutualFollow(Boolean.TRUE);
                    return vo;
                })
                .toList();

        return PageResponse.<FollowUserVo>builder()
                .list(list)
                .page(page)
                .size(size)
                .total(total)
                .hasMore(page * size < total)
                .build();
    }

    /**
     * 校验目标用户是否存在且可被关注。
     */
    private void mustGetActiveUser(Long userId) {
        AppUser target = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getId, userId)
                .eq(AppUser::getIsDeleted, 0)
                .eq(AppUser::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        if (target == null) {
            throw BizException.of(ErrorCode.USER_TARGET_NOT_FOUND);
        }
    }

    /**
     * 关注成功后增加双方计数字段。
     */
    private void increaseFollowCount(Long currentUserId, Long targetUserId) {
        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, currentUserId)
                .setSql("following_count = following_count + 1"));
        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, targetUserId)
                .setSql("follower_count = follower_count + 1"));
    }

    /**
     * 取关后减少双方计数字段，最低不低于 0。
     */
    private void decreaseFollowCount(Long currentUserId, Long targetUserId) {
        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, currentUserId)
                .setSql("following_count = IF(following_count > 0, following_count - 1, 0)"));
        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, targetUserId)
                .setSql("follower_count = IF(follower_count > 0, follower_count - 1, 0)"));
    }

    /**
     * 批量加载用户信息并转成 Map。
     */
    private Map<Long, AppUser> loadUserMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return appUserMapper.selectList(new LambdaQueryWrapper<AppUser>()
                        .in(AppUser::getId, userIds)
                        .eq(AppUser::getIsDeleted, 0))
                .stream()
                .collect(Collectors.toMap(AppUser::getId, Function.identity(), (a, b) -> a));
    }

    /**
     * 计算互关集合（对方也关注我）。
     */
    private Set<Long> loadMutualSet(Long currentUserId, List<Long> userIds) {
        return userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFolloweeId, currentUserId)
                        .eq(UserFollow::getStatus, "ACTIVE")
                        .in(UserFollow::getFollowerId, userIds))
                .stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toSet());
    }

    /**
     * 计算“我已关注 ta”集合。
     */
    private Set<Long> loadFollowedByMeSet(Long currentUserId, List<Long> userIds) {
        return userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, currentUserId)
                        .eq(UserFollow::getStatus, "ACTIVE")
                        .in(UserFollow::getFolloweeId, userIds))
                .stream()
                .map(UserFollow::getFolloweeId)
                .collect(Collectors.toSet());
    }

    /**
     * 用户实体转关注列表 VO。
     */
    private FollowUserVo toVo(AppUser user) {
        FollowUserVo vo = new FollowUserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        return vo;
    }

    /**
     * 通用分页结果组装。
     */
    private PageResponse<FollowUserVo> toPageResponse(Page<UserFollow> result, List<FollowUserVo> list) {
        return PageResponse.<FollowUserVo>builder()
                .list(list)
                .page(result.getCurrent())
                .size(result.getSize())
                .total(result.getTotal())
                .hasMore(result.getCurrent() * result.getSize() < result.getTotal())
                .build();
    }

    /**
     * 空分页返回体。
     */
    private PageResponse<FollowUserVo> emptyPage(Page<UserFollow> result) {
        return PageResponse.<FollowUserVo>builder()
                .list(Collections.emptyList())
                .page(result.getCurrent())
                .size(result.getSize())
                .total(result.getTotal())
                .hasMore(Boolean.FALSE)
                .build();
    }

    /**
     * 关注动作通知：发送给被关注者。
     */
    private void createFollowNotification(Long actorId, Long receiverId) {
        notificationService.create(
                receiverId,
                actorId,
                NotificationType.FOLLOW,
                NotificationTargetType.USER,
                actorId,
                "你有新的关注",
                "有人关注了你"
        );
    }
}
