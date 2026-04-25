package com.community.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.backend.common.api.PageResponse;
import com.community.backend.common.constant.NotificationTargetType;
import com.community.backend.common.constant.NotificationType;
import com.community.backend.common.error.BizException;
import com.community.backend.common.error.ErrorCode;
import com.community.backend.dto.post.CreatePostRequest;
import com.community.backend.dto.post.PostQueryRequest;
import com.community.backend.entity.AppUser;
import com.community.backend.entity.FileObject;
import com.community.backend.entity.Post;
import com.community.backend.entity.PostFavorite;
import com.community.backend.entity.PostLike;
import com.community.backend.entity.PostTagRel;
import com.community.backend.entity.Tag;
import com.community.backend.mapper.AppUserMapper;
import com.community.backend.mapper.FileObjectMapper;
import com.community.backend.mapper.PostFavoriteMapper;
import com.community.backend.mapper.PostLikeMapper;
import com.community.backend.mapper.PostMapper;
import com.community.backend.mapper.PostTagRelMapper;
import com.community.backend.mapper.TagMapper;
import com.community.backend.service.NotificationService;
import com.community.backend.service.PostService;
import com.community.backend.vo.post.PostDetailVo;
import com.community.backend.vo.post.PostListItemVo;
import com.community.backend.vo.post.TagVo;
import com.community.backend.vo.user.UserSummaryVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 帖子服务实现：帖子 CRUD（首批为创建/查询）与点赞收藏互动。
 */
@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final TagMapper tagMapper;
    private final PostTagRelMapper postTagRelMapper;
    private final AppUserMapper appUserMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostFavoriteMapper postFavoriteMapper;
    private final FileObjectMapper fileObjectMapper;
    private final NotificationService notificationService;

    public PostServiceImpl(PostMapper postMapper,
                           TagMapper tagMapper,
                           PostTagRelMapper postTagRelMapper,
                           AppUserMapper appUserMapper,
                           PostLikeMapper postLikeMapper,
                           PostFavoriteMapper postFavoriteMapper,
                           FileObjectMapper fileObjectMapper,
                           NotificationService notificationService) {
        this.postMapper = postMapper;
        this.tagMapper = tagMapper;
        this.postTagRelMapper = postTagRelMapper;
        this.appUserMapper = appUserMapper;
        this.postLikeMapper = postLikeMapper;
        this.postFavoriteMapper = postFavoriteMapper;
        this.fileObjectMapper = fileObjectMapper;
        this.notificationService = notificationService;
    }

    /**
     * 发布帖子：
     * 1) 校验标签有效性
     * 2) 创建帖子主记录
     * 3) 维护标签计数、作者发帖计数、附件绑定关系
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long currentUserId, CreatePostRequest request) {
        LinkedHashSet<Long> uniqueTagIds = normalizeIds(request.getTagIds());
        if (!uniqueTagIds.isEmpty()) {
            long activeTagCount = tagMapper.selectCount(new LambdaQueryWrapper<Tag>()
                    .in(Tag::getId, uniqueTagIds)
                    .eq(Tag::getStatus, "ACTIVE"));
            if (activeTagCount != uniqueTagIds.size()) {
                throw BizException.of(ErrorCode.POST_TAG_INVALID);
            }
        }

        Post post = Post.builder()
                .authorId(currentUserId)
                .title(request.getTitle())
                .excerpt(buildExcerpt(request.getContentMd()))
                .contentMd(request.getContentMd())
                .contentHtml(request.getContentMd())
                .coverUrl(request.getCoverUrl())
                .status("PUBLISHED")
                .allowComment(Boolean.TRUE.equals(request.getAllowComment()) ? 1 : 0)
                .viewCount(0)
                .likeCount(0)
                .favoriteCount(0)
                .commentCount(0)
                .isDeleted(0)
                .publishedAt(LocalDateTime.now())
                .build();
        postMapper.insert(post);

        if (!uniqueTagIds.isEmpty()) {
            for (Long tagId : uniqueTagIds) {
                postTagRelMapper.insert(PostTagRel.builder()
                        .postId(post.getId())
                        .tagId(tagId)
                        .build());
                tagMapper.update(null, new LambdaUpdateWrapper<Tag>()
                        .eq(Tag::getId, tagId)
                        .setSql("post_count = post_count + 1"));
            }
        }

        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, currentUserId)
                .setSql("post_count = post_count + 1"));

        if (!CollectionUtils.isEmpty(request.getAttachmentFileIds())) {
            List<Long> fileIds = request.getAttachmentFileIds().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (!fileIds.isEmpty()) {
                fileObjectMapper.update(null, new LambdaUpdateWrapper<FileObject>()
                        .in(FileObject::getId, fileIds)
                        .eq(FileObject::getUploaderId, currentUserId)
                        .set(FileObject::getBizType, "POST")
                        .set(FileObject::getBizId, post.getId())
                        .set(FileObject::getStatus, "BOUND"));
            }
        }

        return post.getId();
    }

    /**
     * 帖子列表查询：支持作者、标签、关键词、排序。
     */
    @Override
    public PageResponse<PostListItemVo> list(PostQueryRequest query, Long currentUserId) {
        Page<Post> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, "PUBLISHED")
                .eq(Post::getIsDeleted, 0);
        if (query.getAuthorId() != null) {
            wrapper.eq(Post::getAuthorId, query.getAuthorId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Post::getTitle, query.getKeyword())
                    .or()
                    .like(Post::getExcerpt, query.getKeyword()));
        }
        if (query.getTagId() != null) {
            List<Long> postIds = postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRel>()
                            .eq(PostTagRel::getTagId, query.getTagId()))
                    .stream()
                    .map(PostTagRel::getPostId)
                    .toList();
            if (postIds.isEmpty()) {
                return PageResponse.<PostListItemVo>builder()
                        .list(Collections.emptyList())
                        .page(query.getPage())
                        .size(query.getSize())
                        .total(0L)
                        .hasMore(Boolean.FALSE)
                        .build();
            }
            wrapper.in(Post::getId, postIds);
        }
        if ("hot".equalsIgnoreCase(query.getSort())) {
            wrapper.orderByDesc(Post::getLikeCount)
                    .orderByDesc(Post::getCommentCount)
                    .orderByDesc(Post::getPublishedAt);
        } else {
            wrapper.orderByDesc(Post::getPublishedAt);
        }

        Page<Post> result = postMapper.selectPage(page, wrapper);
        if (result.getRecords().isEmpty()) {
            return PageResponse.<PostListItemVo>builder()
                    .list(Collections.emptyList())
                    .page(result.getCurrent())
                    .size(result.getSize())
                    .total(result.getTotal())
                    .hasMore(Boolean.FALSE)
                    .build();
        }

        List<Long> postIds = result.getRecords().stream().map(Post::getId).toList();
        Map<Long, UserSummaryVo> authorMap = loadAuthorMap(result.getRecords().stream().map(Post::getAuthorId).toList());
        Map<Long, List<TagVo>> tagMap = loadTagMapByPostIds(postIds);
        Set<Long> likedSet = loadLikedPostSet(currentUserId, postIds);
        Set<Long> favoritedSet = loadFavoritedPostSet(currentUserId, postIds);

        List<PostListItemVo> list = result.getRecords().stream().map(post -> {
            PostListItemVo vo = new PostListItemVo();
            vo.setId(post.getId());
            vo.setTitle(post.getTitle());
            vo.setExcerpt(post.getExcerpt());
            vo.setCoverUrl(post.getCoverUrl());
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
                .page(result.getCurrent())
                .size(result.getSize())
                .total(result.getTotal())
                .hasMore(result.getCurrent() * result.getSize() < result.getTotal())
                .build();
    }

    /**
     * 帖子详情查询：返回正文、标签、作者及用户互动状态。
     */
    @Override
    public PostDetailVo detail(Long postId, Long currentUserId) {
        Post post = mustGetPublishedPost(postId);

        PostDetailVo vo = new PostDetailVo();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContentMd(post.getContentMd());
        vo.setContentHtml(post.getContentHtml());
        vo.setCoverUrl(post.getCoverUrl());
        vo.setAllowComment(post.getAllowComment());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setFavoriteCount(post.getFavoriteCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setPublishedAt(post.getPublishedAt());
        vo.setAuthor(loadAuthorMap(List.of(post.getAuthorId())).get(post.getAuthorId()));
        vo.setTags(loadTagMapByPostIds(List.of(postId)).getOrDefault(postId, Collections.emptyList()));
        vo.setAttachmentFileIds(loadAttachmentFileIds(postId));
        vo.setLiked(loadLikedPostSet(currentUserId, List.of(postId)).contains(postId));
        vo.setFavorited(loadFavoritedPostSet(currentUserId, List.of(postId)).contains(postId));
        return vo;
    }

    /**
     * 点赞帖子：支持幂等与历史记录恢复。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long currentUserId, Long postId) {
        Post post = mustGetPublishedPost(postId);
        PostLike existing = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUserId, currentUserId)
                .eq(PostLike::getPostId, postId)
                .last("LIMIT 1"));
        if (existing == null) {
            postLikeMapper.insert(PostLike.builder()
                    .userId(currentUserId)
                    .postId(postId)
                    .status("ACTIVE")
                    .build());
            increasePostLikeCount(postId);
            // 新增点赞后触发通知
            createPostLikeNotification(currentUserId, post);
            return;
        }
        if ("ACTIVE".equals(existing.getStatus())) {
            return;
        }
        postLikeMapper.update(null, new LambdaUpdateWrapper<PostLike>()
                .eq(PostLike::getId, existing.getId())
                .set(PostLike::getStatus, "ACTIVE"));
        increasePostLikeCount(postId);
        // 取消后再次点赞同样需要触发通知
        createPostLikeNotification(currentUserId, post);
    }

    /**
     * 取消点赞帖子：仅对 ACTIVE 记录生效。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(Long currentUserId, Long postId) {
        PostLike existing = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUserId, currentUserId)
                .eq(PostLike::getPostId, postId)
                .last("LIMIT 1"));
        if (existing == null || !"ACTIVE".equals(existing.getStatus())) {
            return;
        }
        postLikeMapper.update(null, new LambdaUpdateWrapper<PostLike>()
                .eq(PostLike::getId, existing.getId())
                .set(PostLike::getStatus, "CANCELLED"));
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, postId)
                .setSql("like_count = IF(like_count > 0, like_count - 1, 0)"));
    }

    /**
     * 收藏帖子：支持幂等与历史记录恢复。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favorite(Long currentUserId, Long postId) {
        mustGetPublishedPost(postId);
        PostFavorite existing = postFavoriteMapper.selectOne(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, currentUserId)
                .eq(PostFavorite::getPostId, postId)
                .last("LIMIT 1"));
        if (existing == null) {
            postFavoriteMapper.insert(PostFavorite.builder()
                    .userId(currentUserId)
                    .postId(postId)
                    .status("ACTIVE")
                    .build());
            increasePostFavoriteCount(postId);
            return;
        }
        if ("ACTIVE".equals(existing.getStatus())) {
            return;
        }
        postFavoriteMapper.update(null, new LambdaUpdateWrapper<PostFavorite>()
                .eq(PostFavorite::getId, existing.getId())
                .set(PostFavorite::getStatus, "ACTIVE"));
        increasePostFavoriteCount(postId);
    }

    /**
     * 取消收藏帖子：仅对 ACTIVE 记录生效。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavorite(Long currentUserId, Long postId) {
        PostFavorite existing = postFavoriteMapper.selectOne(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, currentUserId)
                .eq(PostFavorite::getPostId, postId)
                .last("LIMIT 1"));
        if (existing == null || !"ACTIVE".equals(existing.getStatus())) {
            return;
        }
        postFavoriteMapper.update(null, new LambdaUpdateWrapper<PostFavorite>()
                .eq(PostFavorite::getId, existing.getId())
                .set(PostFavorite::getStatus, "CANCELLED"));
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, postId)
                .setSql("favorite_count = IF(favorite_count > 0, favorite_count - 1, 0)"));
    }

    /**
     * 帖子点赞数 +1。
     */
    private void increasePostLikeCount(Long postId) {
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, postId)
                .setSql("like_count = like_count + 1"));
    }

    /**
     * 帖子收藏数 +1。
     */
    private void increasePostFavoriteCount(Long postId) {
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, postId)
                .setSql("favorite_count = favorite_count + 1"));
    }

    /**
     * 校验帖子是否为可见发布态。
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
     * 归一化 ID 列表：去空、去重并保持顺序。
     */
    private LinkedHashSet<Long> normalizeIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new LinkedHashSet<>();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 生成帖子摘要（最长 120 字符）。
     */
    private String buildExcerpt(String contentMd) {
        if (!StringUtils.hasText(contentMd)) {
            return "";
        }
        String raw = contentMd.replaceAll("\\s+", " ").trim();
        if (raw.length() <= 120) {
            return raw;
        }
        return raw.substring(0, 120);
    }

    /**
     * 批量加载作者摘要信息。
     */
    private Map<Long, UserSummaryVo> loadAuthorMap(List<Long> authorIds) {
        if (CollectionUtils.isEmpty(authorIds)) {
            return Collections.emptyMap();
        }
        List<AppUser> users = appUserMapper.selectList(new LambdaQueryWrapper<AppUser>()
                .in(AppUser::getId, new ArrayList<>(new LinkedHashSet<>(authorIds)))
                .eq(AppUser::getIsDeleted, 0));
        return users.stream().map(user -> {
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
     * 加载帖子-标签映射。
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
            result.computeIfAbsent(rel.getPostId(), k -> new ArrayList<>()).add(tagVo);
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
     * 加载帖子已绑定附件 ID（按排序字段升序）。
     */
    private List<Long> loadAttachmentFileIds(Long postId) {
        return fileObjectMapper.selectList(new LambdaQueryWrapper<FileObject>()
                        .eq(FileObject::getBizType, "POST")
                        .eq(FileObject::getBizId, postId)
                        .eq(FileObject::getStatus, "BOUND")
                        .orderByAsc(FileObject::getSortOrder))
                .stream()
                .map(FileObject::getId)
                .toList();
    }

    /**
     * 帖子点赞通知：发送给帖主。
     */
    private void createPostLikeNotification(Long actorId, Post post) {
        notificationService.create(
                post.getAuthorId(),
                actorId,
                NotificationType.POST_LIKE,
                NotificationTargetType.POST,
                post.getId(),
                "你的帖子收到点赞",
                "有人赞了你的帖子《" + post.getTitle() + "》"
        );
    }
}
