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
import com.community.backend.entity.Post;
import com.community.backend.entity.PostFavorite;
import com.community.backend.entity.PostLike;
import com.community.backend.entity.PostTagRel;
import com.community.backend.entity.Tag;
import com.community.backend.entity.UserFollow;
import com.community.backend.mapper.AppUserMapper;
import com.community.backend.mapper.PostFavoriteMapper;
import com.community.backend.mapper.PostLikeMapper;
import com.community.backend.mapper.PostMapper;
import com.community.backend.mapper.PostTagRelMapper;
import com.community.backend.mapper.TagMapper;
import com.community.backend.mapper.UserFollowMapper;
import com.community.backend.service.UserService;
import com.community.backend.vo.post.PostListItemVo;
import com.community.backend.vo.post.TagVo;
import com.community.backend.vo.user.UserProfileVo;
import com.community.backend.vo.user.UserSummaryVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户服务实现：用户主页、搜索与资料编辑。
 */
@Service
public class UserServiceImpl implements UserService {

    private final AppUserMapper appUserMapper;
    private final UserFollowMapper userFollowMapper;
    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final PostTagRelMapper postTagRelMapper;
    private final TagMapper tagMapper;

    public UserServiceImpl(AppUserMapper appUserMapper,
                           UserFollowMapper userFollowMapper,
                           PostMapper postMapper,
                           PostLikeMapper postLikeMapper,
                           PostFavoriteMapper postFavoriteMapper,
                           PostTagRelMapper postTagRelMapper,
                           TagMapper tagMapper) {
        this.appUserMapper = appUserMapper;
        this.userFollowMapper = userFollowMapper;
        this.postMapper = postMapper;
        this.postLikeMapper = postLikeMapper;
        this.postFavoriteMapper = postFavoriteMapper;
        this.postTagRelMapper = postTagRelMapper;
        this.tagMapper = tagMapper;
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
     * 用户已发布帖子列表：支持游客访问。
     */
    @Override
    public PageResponse<PostListItemVo> listUserPosts(Long userId, Long currentUserId, Long page, Long size) {
        mustGetUser(userId);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getAuthorId, userId)
                .eq(Post::getIsDeleted, 0)
                .orderByDesc(Post::getPublishedAt)
                .orderByDesc(Post::getUpdatedAt);
        if (!Objects.equals(userId, currentUserId)) {
            wrapper.eq(Post::getStatus, "PUBLISHED");
        }
        Page<Post> pager = postMapper.selectPage(new Page<>(page, size), wrapper);
        return buildPostPageFromPosts(pager.getRecords(), pager.getCurrent(), pager.getSize(), pager.getTotal(), currentUserId);
    }

    /**
     * 用户收藏帖子列表：仅允许本人查询。
     */
    @Override
    public PageResponse<PostListItemVo> listUserFavorites(Long userId, Long currentUserId, Long page, Long size) {
        checkSelfAccess(userId, currentUserId);
        Page<PostFavorite> pager = postFavoriteMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, userId)
                .eq(PostFavorite::getStatus, "ACTIVE")
                .orderByDesc(PostFavorite::getUpdatedAt));
        if (pager.getRecords().isEmpty()) {
            return emptyPostPage(pager.getCurrent(), pager.getSize(), pager.getTotal());
        }

        List<Long> postIds = pager.getRecords().stream().map(PostFavorite::getPostId).toList();
        Map<Long, Post> postMap = loadVisiblePostMap(postIds);
        List<Post> orderedPosts = postIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .toList();
        return buildPostPageFromPosts(orderedPosts, pager.getCurrent(), pager.getSize(), pager.getTotal(), currentUserId);
    }

    /**
     * 用户点赞帖子列表：仅允许本人查询。
     */
    @Override
    public PageResponse<PostListItemVo> listUserLikes(Long userId, Long currentUserId, Long page, Long size) {
        checkSelfAccess(userId, currentUserId);
        Page<PostLike> pager = postLikeMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUserId, userId)
                .eq(PostLike::getStatus, "ACTIVE")
                .orderByDesc(PostLike::getUpdatedAt));
        if (pager.getRecords().isEmpty()) {
            return emptyPostPage(pager.getCurrent(), pager.getSize(), pager.getTotal());
        }

        List<Long> postIds = pager.getRecords().stream().map(PostLike::getPostId).toList();
        Map<Long, Post> postMap = loadVisiblePostMap(postIds);
        List<Post> orderedPosts = postIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .toList();
        return buildPostPageFromPosts(orderedPosts, pager.getCurrent(), pager.getSize(), pager.getTotal(), currentUserId);
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
     * 校验用户存在（用于用户中心列表）。
     */
    private AppUser mustGetUser(Long userId) {
        AppUser user = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getId, userId)
                .eq(AppUser::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (user == null) {
            throw BizException.of(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    /**
     * 收藏/点赞列表权限校验：仅本人可查。
     */
    private void checkSelfAccess(Long userId, Long currentUserId) {
        if (!Objects.equals(userId, currentUserId)) {
            throw BizException.of(ErrorCode.AUTH_ACCESS_DENIED);
        }
        mustGetUser(userId);
    }

    /**
     * 按 ID 批量加载可见帖子，并转为 Map。
     */
    private Map<Long, Post> loadVisiblePostMap(List<Long> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }
        return postMapper.selectList(new LambdaQueryWrapper<Post>()
                        .in(Post::getId, postIds)
                        .eq(Post::getIsDeleted, 0)
                        .eq(Post::getStatus, "PUBLISHED"))
                .stream()
                .collect(Collectors.toMap(Post::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * 将帖子列表组装成用户中心列表分页结果（附作者、标签、点赞收藏状态）。
     */
    private PageResponse<PostListItemVo> buildPostPageFromPosts(List<Post> posts,
                                                                Long page,
                                                                Long size,
                                                                Long total,
                                                                Long currentUserId) {
        if (posts.isEmpty()) {
            return emptyPostPage(page, size, total);
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, UserSummaryVo> authorMap = loadAuthorMap(posts.stream().map(Post::getAuthorId).toList());
        Map<Long, List<TagVo>> tagMap = loadTagMapByPostIds(postIds);
        Set<Long> likedSet = loadLikedPostSet(currentUserId, postIds);
        Set<Long> favoritedSet = loadFavoritedPostSet(currentUserId, postIds);

        List<PostListItemVo> list = posts.stream().map(post -> {
            PostListItemVo vo = new PostListItemVo();
            vo.setId(post.getId());
            vo.setTitle(post.getTitle());
            vo.setExcerpt(post.getExcerpt());
            vo.setCoverUrl(post.getCoverUrl());
            vo.setStatus(post.getStatus());
            vo.setViewCount(post.getViewCount());
            vo.setLikeCount(post.getLikeCount());
            vo.setFavoriteCount(post.getFavoriteCount());
            vo.setCommentCount(post.getCommentCount());
            vo.setPublishedAt(post.getPublishedAt());
            vo.setAuthor(authorMap.get(post.getAuthorId()));
            vo.setTags(tagMap.getOrDefault(post.getId(), Collections.emptyList()));
            vo.setLiked(likedSet.contains(post.getId()));
            vo.setFavorited(favoritedSet.contains(post.getId()));
            return vo;
        }).toList();

        return PageResponse.<PostListItemVo>builder()
                .list(list)
                .page(page)
                .size(size)
                .total(total)
                .hasMore(page * size < total)
                .build();
    }

    /**
     * 空帖子分页返回。
     */
    private PageResponse<PostListItemVo> emptyPostPage(Long page, Long size, Long total) {
        return PageResponse.<PostListItemVo>builder()
                .list(Collections.emptyList())
                .page(page)
                .size(size)
                .total(total)
                .hasMore(Boolean.FALSE)
                .build();
    }

    /**
     * 批量加载作者摘要信息。
     */
    private Map<Long, UserSummaryVo> loadAuthorMap(List<Long> authorIds) {
        if (CollectionUtils.isEmpty(authorIds)) {
            return Collections.emptyMap();
        }
        return appUserMapper.selectList(new LambdaQueryWrapper<AppUser>()
                        .in(AppUser::getId, authorIds)
                        .eq(AppUser::getIsDeleted, 0))
                .stream()
                .map(this::toSummaryVo)
                .collect(Collectors.toMap(UserSummaryVo::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * 加载帖子-标签映射关系。
     */
    private Map<Long, List<TagVo>> loadTagMapByPostIds(List<Long> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyMap();
        }
        List<PostTagRel> rels = postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRel>()
                .in(PostTagRel::getPostId, postIds));
        if (rels.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> tagIds = rels.stream().map(PostTagRel::getTagId).distinct().toList();
        Map<Long, TagVo> tagVoMap = tagMapper.selectList(new LambdaQueryWrapper<Tag>().in(Tag::getId, tagIds)).stream()
                .collect(Collectors.toMap(Tag::getId, tag -> {
                    TagVo vo = new TagVo();
                    vo.setId(tag.getId());
                    vo.setName(tag.getName());
                    return vo;
                }, (a, b) -> a));

        Map<Long, List<TagVo>> result = new LinkedHashMap<>();
        for (PostTagRel rel : rels) {
            TagVo tagVo = tagVoMap.get(rel.getTagId());
            if (tagVo == null) {
                continue;
            }
            result.computeIfAbsent(rel.getPostId(), k -> new java.util.ArrayList<>()).add(tagVo);
        }
        return result;
    }

    /**
     * 加载当前用户点赞过的帖子集合。
     */
    private Set<Long> loadLikedPostSet(Long currentUserId, List<Long> postIds) {
        if (currentUserId == null || CollectionUtils.isEmpty(postIds)) {
            return Collections.emptySet();
        }
        return postLikeMapper.selectList(new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getUserId, currentUserId)
                        .eq(PostLike::getStatus, "ACTIVE")
                        .in(PostLike::getPostId, postIds))
                .stream()
                .map(PostLike::getPostId)
                .collect(Collectors.toSet());
    }

    /**
     * 加载当前用户收藏过的帖子集合。
     */
    private Set<Long> loadFavoritedPostSet(Long currentUserId, List<Long> postIds) {
        if (currentUserId == null || CollectionUtils.isEmpty(postIds)) {
            return Collections.emptySet();
        }
        return postFavoriteMapper.selectList(new LambdaQueryWrapper<PostFavorite>()
                        .eq(PostFavorite::getUserId, currentUserId)
                        .eq(PostFavorite::getStatus, "ACTIVE")
                        .in(PostFavorite::getPostId, postIds))
                .stream()
                .map(PostFavorite::getPostId)
                .collect(Collectors.toSet());
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
