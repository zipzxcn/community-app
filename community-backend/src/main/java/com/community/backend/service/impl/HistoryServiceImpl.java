package com.community.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.backend.common.api.PageResponse;
import com.community.backend.common.error.BizException;
import com.community.backend.common.error.ErrorCode;
import com.community.backend.entity.AppUser;
import com.community.backend.entity.Post;
import com.community.backend.entity.UserBrowseHistory;
import com.community.backend.mapper.AppUserMapper;
import com.community.backend.mapper.PostMapper;
import com.community.backend.mapper.UserBrowseHistoryMapper;
import com.community.backend.service.HistoryService;
import com.community.backend.vo.history.HistoryItemVo;
import com.community.backend.vo.user.UserSummaryVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 浏览历史服务实现：浏览记录、分页查询与删除清理。
 */
@Service
public class HistoryServiceImpl implements HistoryService {

    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final PostMapper postMapper;
    private final AppUserMapper appUserMapper;

    public HistoryServiceImpl(UserBrowseHistoryMapper userBrowseHistoryMapper,
                              PostMapper postMapper,
                              AppUserMapper appUserMapper) {
        this.userBrowseHistoryMapper = userBrowseHistoryMapper;
        this.postMapper = postMapper;
        this.appUserMapper = appUserMapper;
    }

    /**
     * 浏览历史列表：按最后浏览时间倒序返回。
     */
    @Override
    public PageResponse<HistoryItemVo> list(Long currentUserId, Long page, Long size) {
        Page<UserBrowseHistory> pager = new Page<>(page, size);
        Page<UserBrowseHistory> result = userBrowseHistoryMapper.selectPage(pager, new LambdaQueryWrapper<UserBrowseHistory>()
                .eq(UserBrowseHistory::getUserId, currentUserId)
                .orderByDesc(UserBrowseHistory::getLastViewedAt));
        if (result.getRecords().isEmpty()) {
            return PageResponse.<HistoryItemVo>builder()
                    .list(Collections.emptyList())
                    .page(result.getCurrent())
                    .size(result.getSize())
                    .total(result.getTotal())
                    .hasMore(Boolean.FALSE)
                    .build();
        }

        Map<Long, Post> postMap = loadPostMap(result.getRecords());
        Map<Long, UserSummaryVo> authorMap = loadAuthorMap(postMap);
        List<HistoryItemVo> list = result.getRecords().stream()
                .map(history -> toVo(history, postMap.get(history.getPostId()), authorMap))
                .filter(item -> item.getPostId() != null)
                .toList();
        return PageResponse.<HistoryItemVo>builder()
                .list(list)
                .page(result.getCurrent())
                .size(result.getSize())
                .total(result.getTotal())
                .hasMore(result.getCurrent() * result.getSize() < result.getTotal())
                .build();
    }

    /**
     * 记录浏览历史：
     * 1) 校验帖子可见
     * 2) 已存在则次数+1，否则新增
     * 3) 同步帖子 view_count
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void record(Long currentUserId, Long postId) {
        mustGetPublishedPost(postId);
        UserBrowseHistory existing = userBrowseHistoryMapper.selectOne(new LambdaQueryWrapper<UserBrowseHistory>()
                .eq(UserBrowseHistory::getUserId, currentUserId)
                .eq(UserBrowseHistory::getPostId, postId)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            userBrowseHistoryMapper.insert(UserBrowseHistory.builder()
                    .userId(currentUserId)
                    .postId(postId)
                    .viewCount(1)
                    .lastViewedAt(now)
                    .build());
        } else {
            userBrowseHistoryMapper.update(null, new LambdaUpdateWrapper<UserBrowseHistory>()
                    .eq(UserBrowseHistory::getId, existing.getId())
                    .setSql("view_count = view_count + 1")
                    .set(UserBrowseHistory::getLastViewedAt, now));
        }

        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, postId)
                .setSql("view_count = view_count + 1"));
    }

    /**
     * 删除单条历史：仅允许删除自己的记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long currentUserId, Long historyId) {
        int affected = userBrowseHistoryMapper.delete(new LambdaQueryWrapper<UserBrowseHistory>()
                .eq(UserBrowseHistory::getId, historyId)
                .eq(UserBrowseHistory::getUserId, currentUserId));
        if (affected == 0) {
            throw BizException.of(ErrorCode.HISTORY_NOT_FOUND);
        }
    }

    /**
     * 清空当前用户浏览历史。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clear(Long currentUserId) {
        userBrowseHistoryMapper.delete(new LambdaQueryWrapper<UserBrowseHistory>()
                .eq(UserBrowseHistory::getUserId, currentUserId));
    }

    /**
     * 校验帖子为可见发布态。
     */
    private Post mustGetPublishedPost(Long postId) {
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId)
                .eq(Post::getIsDeleted, 0)
                .eq(Post::getStatus, "PUBLISHED")
                .last("LIMIT 1"));
        if (post == null) {
            throw BizException.of(ErrorCode.POST_NOT_FOUND);
        }
        return post;
    }

    /**
     * 加载浏览历史对应帖子。
     */
    private Map<Long, Post> loadPostMap(List<UserBrowseHistory> histories) {
        List<Long> postIds = histories.stream().map(UserBrowseHistory::getPostId).distinct().toList();
        if (postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return postMapper.selectList(new LambdaQueryWrapper<Post>()
                        .in(Post::getId, postIds)
                        .eq(Post::getIsDeleted, 0))
                .stream()
                .collect(Collectors.toMap(Post::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * 加载帖子作者摘要。
     */
    private Map<Long, UserSummaryVo> loadAuthorMap(Map<Long, Post> postMap) {
        if (postMap.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> authorIds = postMap.values().stream().map(Post::getAuthorId).distinct().toList();
        return appUserMapper.selectList(new LambdaQueryWrapper<AppUser>()
                        .in(AppUser::getId, new ArrayList<>(authorIds))
                        .eq(AppUser::getIsDeleted, 0))
                .stream()
                .map(user -> {
                    UserSummaryVo vo = new UserSummaryVo();
                    vo.setId(user.getId());
                    vo.setUsername(user.getUsername());
                    vo.setNickname(user.getNickname());
                    vo.setAvatarUrl(user.getAvatarUrl());
                    return vo;
                })
                .collect(Collectors.toMap(UserSummaryVo::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * 历史实体转返回 VO。
     */
    private HistoryItemVo toVo(UserBrowseHistory history, Post post, Map<Long, UserSummaryVo> authorMap) {
        HistoryItemVo vo = new HistoryItemVo();
        vo.setId(history.getId());
        vo.setViewCount(history.getViewCount());
        vo.setLastViewedAt(history.getLastViewedAt());
        if (post == null) {
            return vo;
        }
        vo.setPostId(post.getId());
        vo.setPostTitle(post.getTitle());
        vo.setPostCoverUrl(post.getCoverUrl());
        vo.setAuthor(authorMap.get(post.getAuthorId()));
        return vo;
    }
}
