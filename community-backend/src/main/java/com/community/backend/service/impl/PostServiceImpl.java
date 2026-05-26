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
import com.community.backend.dto.post.UpdatePostRequest;
import com.community.backend.entity.AppUser;
import com.community.backend.entity.FileObject;
import com.community.backend.entity.Post;
import com.community.backend.entity.PostFavorite;
import com.community.backend.entity.PostLike;
import com.community.backend.entity.PostTagRel;
import com.community.backend.entity.Tag;
import com.community.backend.entity.UserBrowseHistory;
import com.community.backend.entity.UserFollow;
import com.community.backend.mapper.AppUserMapper;
import com.community.backend.mapper.FileObjectMapper;
import com.community.backend.mapper.PostFavoriteMapper;
import com.community.backend.mapper.PostLikeMapper;
import com.community.backend.mapper.PostMapper;
import com.community.backend.mapper.PostTagRelMapper;
import com.community.backend.mapper.TagMapper;
import com.community.backend.mapper.UserBrowseHistoryMapper;
import com.community.backend.mapper.UserFollowMapper;
import com.community.backend.service.NotificationService;
import com.community.backend.service.PostService;
import com.community.backend.vo.file.FileObjectVo;
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
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 帖子服务实现：
 * - 负责帖子从创建、编辑、显隐、删除，到点赞/收藏/详情组装的完整生命周期。
 * - 还承担标签、附件、通知、统计字段同步等横切逻辑。
 * - 适合作为学习“聚合根 + 关联表 + 统计字段 + 通知副作用”这一类业务服务的样例。
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
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final UserFollowMapper userFollowMapper;

    public PostServiceImpl(PostMapper postMapper,
                           TagMapper tagMapper,
                           PostTagRelMapper postTagRelMapper,
                           AppUserMapper appUserMapper,
                           PostLikeMapper postLikeMapper,
                           PostFavoriteMapper postFavoriteMapper,
                           FileObjectMapper fileObjectMapper,
                           NotificationService notificationService,
                           UserBrowseHistoryMapper userBrowseHistoryMapper,
                           UserFollowMapper userFollowMapper) {
        this.postMapper = postMapper;
        this.tagMapper = tagMapper;
        this.postTagRelMapper = postTagRelMapper;
        this.appUserMapper = appUserMapper;
        this.postLikeMapper = postLikeMapper;
        this.postFavoriteMapper = postFavoriteMapper;
        this.fileObjectMapper = fileObjectMapper;
        this.notificationService = notificationService;
        this.userBrowseHistoryMapper = userBrowseHistoryMapper;
        this.userFollowMapper = userFollowMapper;
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
        // 注意1：先把输入标签做“去空、去重、保序”归一化，后面所有逻辑都基于干净数据展开。
        LinkedHashSet<Long> uniqueTagIds = normalizeIds(request.getTagIds());
        // 注意2：在真正写帖子前校验标签合法性，避免出现“帖子已创建成功但标签关联失败”的半成功状态。
        validateTagIds(uniqueTagIds);

        // 帖子主表保存的是编辑核心字段，标签与附件关系通过关联表/文件表维护。
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
        // 注意3：先插入主表拿到 postId，后续标签关系、附件绑定、通知都依赖这个主键。
        postMapper.insert(post);

        // 注意4：标签关系是典型的多对多，这里通过中间表 post_tag_rel 落地。
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

        // 注意5：很多列表页直接展示 postCount，所以这里同步维护冗余计数字段，换取查询时的简单与高效。
        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, currentUserId)
                .setSql("post_count = post_count + 1"));

        // 注意6：附件不是直接塞进帖子表，而是通过 FileObject 的 bizType/bizId 反向绑定到帖子。
        if (!CollectionUtils.isEmpty(request.getAttachmentFileIds())) {
            List<Long> fileIds = request.getAttachmentFileIds().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            // 注意7：绑定文件时，确保文件是当前用户上传的，避免跨用户操作。
            if (!fileIds.isEmpty()) {
                fileObjectMapper.update(null, new LambdaUpdateWrapper<FileObject>()
                        .in(FileObject::getId, fileIds)
                        .eq(FileObject::getUploaderId, currentUserId)
                        .set(FileObject::getBizType, "POST")
                        .set(FileObject::getBizId, post.getId())
                        .set(FileObject::getStatus, "BOUND"));
            }
        }

        // 新帖子发布后通知粉丝，帮助内容更快被关注者看到。
        createFollowersPostNotifications(post);

        return post.getId();
    }

    /**
     * 帖子列表查询：支持作者、标签、关键词、排序。
     */
    @Override
    public PageResponse<PostListItemVo> list(PostQueryRequest query, Long currentUserId) {
        Page<Post> page = new Page<>(query.getPage(), query.getSize()); // 分页查询
        // 只查询已发布的帖子，避免返回草稿状态的帖子。
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, "PUBLISHED")
                .eq(Post::getIsDeleted, 0);
        if (query.getAuthorId() != null) {
            wrapper.eq(Post::getAuthorId, query.getAuthorId());
        }
        // 模糊搜索标题与摘要，支持中文与英文。
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Post::getTitle, query.getKeyword())
                    .or()
                    .like(Post::getExcerpt, query.getKeyword()));
        }
        // 根签筛选：根据指定标签 ID 筛选帖子。
        if (query.getTagId() != null) {
            List<Long> postIds = postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRel>()
                            .eq(PostTagRel::getTagId, query.getTagId()))
                    .stream()
                    .map(PostTagRel::getPostId)
                    .toList();
            if (postIds.isEmpty()) {
                // 如果指定标签没有帖子，直接返回空列表。
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
        // 排序规则：控制列表按最新或热门等方式返回。
        if ("hot".equalsIgnoreCase(query.getSort())) {
            wrapper.orderByDesc(Post::getLikeCount)
                    .orderByDesc(Post::getCommentCount)
                    .orderByDesc(Post::getPublishedAt);
        } else {
            wrapper.orderByDesc(Post::getPublishedAt);
        }

        // 注意：列表查询先尽量在数据库层完成筛选和排序，减少把大量无关数据拉到内存里再处理。
        Page<Post> result = postMapper.selectPage(page, wrapper); // 分页查询帖子
        if (result.getRecords().isEmpty()) {
            return PageResponse.<PostListItemVo>builder()
                    .list(Collections.emptyList())
                    .page(result.getCurrent())
                    .size(result.getSize())
                    .total(result.getTotal())
                    .hasMore(Boolean.FALSE)
                    .build();
        }

        List<Long> postIds = result.getRecords().stream().map(Post::getId).toList(); // 提取帖子 ID 列表
        // 注意：列表页需要作者、标签、点赞收藏状态等“附加信息”，这里采用批量预加载，避免 N+1 查询。
        Map<Long, UserSummaryVo> authorMap = loadAuthorMap(result.getRecords().stream().map(Post::getAuthorId).toList()); // 批量加载作者信息
        Map<Long, List<TagVo>> tagMap = loadTagMapByPostIds(postIds); // 批量加载标签信息
        Set<Long> likedSet = loadLikedPostSet(currentUserId, postIds); // 批量加载点赞状态
        Set<Long> favoritedSet = loadFavoritedPostSet(currentUserId, postIds); // 批量加载收藏状态

        List<PostListItemVo> list = result.getRecords().stream().map(post -> {
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
            vo.setAttachmentFiles(loadAttachmentFiles(post.getId()));
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

        // 注意：详情页返回体通常不是直接把实体原样返回，而是组装成更贴近前端消费习惯的 VO。
        PostDetailVo vo = new PostDetailVo();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContentMd(post.getContentMd());
        vo.setContentHtml(post.getContentHtml());
        vo.setCoverUrl(post.getCoverUrl());
        vo.setStatus(post.getStatus());
        vo.setAllowComment(post.getAllowComment());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setFavoriteCount(post.getFavoriteCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setPublishedAt(post.getPublishedAt());
        vo.setAuthor(loadAuthorMap(List.of(post.getAuthorId())).get(post.getAuthorId()));
        vo.setTags(loadTagMapByPostIds(List.of(postId)).getOrDefault(postId, Collections.emptyList()));
        vo.setAttachmentFileIds(loadAttachmentFileIds(postId));
        vo.setAttachmentFiles(loadAttachmentFiles(postId));
        vo.setLiked(loadLikedPostSet(currentUserId, List.of(postId)).contains(postId));
        vo.setFavorited(loadFavoritedPostSet(currentUserId, List.of(postId)).contains(postId));
        return vo;
    }

    /**
     * 首页推荐：
     * - 登录用户优先综合浏览历史、点赞/收藏标签、关注作者做个性化推荐
     * - 未登录用户或个性化信号不足时退化为热门帖子
     */
    @Override
    public List<PostListItemVo> recommend(Long currentUserId, Integer size) {
        int limit = Math.min(Math.max(size == null ? 6 : size, 3), 12);
        if (currentUserId == null) {
            return buildRecommendationVoList(loadTopHotPosts(limit), null, Collections.emptyMap());
        }

        List<Long> recentHistoryPostIds = loadRecentHistoryPostIds(currentUserId, 24);
        Set<Long> recentHistorySet = new LinkedHashSet<>(recentHistoryPostIds);
        Set<Long> interestTagIds = loadInterestTagIds(currentUserId, recentHistoryPostIds);
        Set<Long> followedAuthorIds = loadFollowedAuthorIds(currentUserId);

        Map<Long, Integer> scoreMap = new HashMap<>();
        Map<Long, List<String>> reasonMap = new HashMap<>();

        mergeRecommendationScore(
                scoreMap,
                reasonMap,
                loadLatestPostsByAuthors(followedAuthorIds, currentUserId, 18),
                60,
                "你关注的作者更新了"
        );
        mergeRecommendationScore(
                scoreMap,
                reasonMap,
                loadPostsByTagIds(interestTagIds, currentUserId, 36),
                36,
                "匹配你常看的标签"
        );
        mergeRecommendationScore(
                scoreMap,
                reasonMap,
                loadTopHotPosts(18),
                18,
                "近期热门内容"
        );

        recentHistorySet.forEach(scoreMap::remove);

        List<Long> rankedPostIds = scoreMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .map(Map.Entry::getKey)
                .limit(limit)
                .toList();

        List<Post> rankedPosts = loadPublishedPostsByIds(rankedPostIds);
        if (rankedPosts.size() < limit) {
            Set<Long> excludeIds = new HashSet<>(recentHistorySet);
            excludeIds.addAll(rankedPostIds);
            List<Post> hotFallback = loadTopHotPosts(limit * 3).stream()
                    .filter(post -> !excludeIds.contains(post.getId()))
                    .limit(limit - rankedPosts.size())
                    .toList();
            rankedPosts = new ArrayList<>(rankedPosts);
            rankedPosts.addAll(hotFallback);
        }
        return buildRecommendationVoList(rankedPosts, currentUserId, reasonMap);
    }

    /**
     * 点赞帖子：支持幂等与历史记录恢复。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long currentUserId, Long postId) {
        Post post = mustGetPublishedPost(postId);
        // 检查用户是否已点赞该帖子
        PostLike existing = postLikeMapper.selectOne(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUserId, currentUserId)
                .eq(PostLike::getPostId, postId)
                .last("LIMIT 1"));
        // 注意：点赞/收藏都采用“先查关系表，再决定插入、恢复还是忽略”的幂等写法。
        if (existing == null) {
            // 未点赞，新增点赞记录
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
            // 已点赞，无需重复操作
            return;
        }
        // 已取消点赞，更新状态为 ACTIVE 进行点赞操作
        postLikeMapper.update(null, new LambdaUpdateWrapper<PostLike>()
                .eq(PostLike::getId, existing.getId())
                .set(PostLike::getStatus, "ACTIVE"));
        increasePostLikeCount(postId); // 帖子点赞+1
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
        // 已点赞，更新状态为 CANCELLED 进行取消点赞操作
        postLikeMapper.update(null, new LambdaUpdateWrapper<PostLike>()
                .eq(PostLike::getId, existing.getId())
                .set(PostLike::getStatus, "CANCELLED"));
        // 取消点赞后触发通知，更新帖子点赞数
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
     * 编辑帖子：仅作者可修改，支持正文/封面/评论开关/标签/附件更新。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long currentUserId, Long postId, UpdatePostRequest request) {
        Post post = mustGetOwnedPost(currentUserId, postId);

        // 注意：编辑帖子只改核心字段；标签和附件属于独立关系，需要单独同步，避免一次 update 过于复杂。
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, post.getId())
                .set(Post::getTitle, request.getTitle())
                .set(Post::getExcerpt, buildExcerpt(request.getContentMd()))
                .set(Post::getContentMd, request.getContentMd())
                .set(Post::getContentHtml, request.getContentMd())
                .set(Post::getCoverUrl, request.getCoverUrl())
                .set(Post::getAllowComment, Boolean.TRUE.equals(request.getAllowComment()) ? 1 : 0));

        // 传 tagIds 才同步标签关系；不传视为“不改标签”。
        if (request.getTagIds() != null) {
            syncPostTags(postId, request.getTagIds());
        }
        // 传 attachmentFileIds 才同步附件关系；不传视为“不改附件”。
        if (request.getAttachmentFileIds() != null) {
            syncPostAttachments(currentUserId, postId, request.getAttachmentFileIds());
        }
    }

    /**
     * 软删除帖子：仅作者可删，同时维护用户发帖计数与标签计数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long currentUserId, Long postId) {
        Post post = mustGetOwnedPost(currentUserId, postId);

        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, post.getId())
                .eq(Post::getIsDeleted, 0)
                .set(Post::getIsDeleted, 1)
                .set(Post::getDeletedAt, LocalDateTime.now()));

        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, currentUserId)
                .setSql("post_count = IF(post_count > 0, post_count - 1, 0)"));

        List<Long> tagIds = postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRel>()
                        .eq(PostTagRel::getPostId, postId))
                .stream()
                .map(PostTagRel::getTagId)
                .toList();
        if (!tagIds.isEmpty()) {
            for (Long tagId : new LinkedHashSet<>(tagIds)) {
                tagMapper.update(null, new LambdaUpdateWrapper<Tag>()
                        .eq(Tag::getId, tagId)
                        .setSql("post_count = IF(post_count > 0, post_count - 1, 0)"));
            }
        }

        // 删除帖子时解绑附件，避免后续详情仍能读取到旧绑定。
        fileObjectMapper.update(null, new LambdaUpdateWrapper<FileObject>()
                .eq(FileObject::getBizType, "POST")
                .eq(FileObject::getBizId, postId)
                .eq(FileObject::getUploaderId, currentUserId)
                .set(FileObject::getBizType, null)
                .set(FileObject::getBizId, null)
                .set(FileObject::getStatus, "UPLOADED"));
    }

    /**
     * 显隐帖子：仅作者可操作，hidden=true 下架，hidden=false 上架。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hide(Long currentUserId, Long postId, boolean hidden) {
        Post post = mustGetOwnedPost(currentUserId, postId);
        String targetStatus = hidden ? "HIDDEN" : "PUBLISHED";
        if (targetStatus.equals(post.getStatus())) {
            return;
        }
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, post.getId())
                .set(Post::getStatus, targetStatus));
    }

    /**
     * 标签列表：仅返回 ACTIVE 标签，按帖子数量降序。
     */
    @Override
    public List<TagVo> listTags() {
        return tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getStatus, "ACTIVE")
                        .orderByDesc(Tag::getPostCount)
                        .orderByAsc(Tag::getId))
                .stream()
                .map(tag -> {
                    TagVo vo = new TagVo();
                    vo.setId(tag.getId());
                    vo.setName(tag.getName());
                    return vo;
                })
                .toList();
    }

    private List<PostListItemVo> buildRecommendationVoList(List<Post> posts,
                                                           Long currentUserId,
                                                           Map<Long, List<String>> reasonMap) {
        if (CollectionUtils.isEmpty(posts)) {
            return Collections.emptyList();
        }
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, UserSummaryVo> authorMap = loadAuthorMap(posts.stream().map(Post::getAuthorId).toList());
        Map<Long, List<TagVo>> tagMap = loadTagMapByPostIds(postIds);
        Set<Long> likedSet = loadLikedPostSet(currentUserId, postIds);
        Set<Long> favoritedSet = loadFavoritedPostSet(currentUserId, postIds);

        return posts.stream().map(post -> {
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
            vo.setAttachmentFiles(loadAttachmentFiles(post.getId()));
            vo.setLiked(likedSet.contains(post.getId()));
            vo.setFavorited(favoritedSet.contains(post.getId()));
            List<String> reasons = reasonMap.get(post.getId());
            vo.setRecommendReason(CollectionUtils.isEmpty(reasons) ? "近期热门内容" : reasons.get(0));
            return vo;
        }).toList();
    }

    private void mergeRecommendationScore(Map<Long, Integer> scoreMap,
                                          Map<Long, List<String>> reasonMap,
                                          List<Post> posts,
                                          int baseScore,
                                          String reason) {
        if (CollectionUtils.isEmpty(posts)) {
            return;
        }
        for (Post post : posts) {
            int popularityScore = (post.getLikeCount() == null ? 0 : post.getLikeCount()) * 3
                    + (post.getFavoriteCount() == null ? 0 : post.getFavoriteCount()) * 4
                    + (post.getCommentCount() == null ? 0 : post.getCommentCount()) * 2
                    + (post.getViewCount() == null ? 0 : post.getViewCount()) / 12;
            int recencyBonus = 0;
            if (post.getPublishedAt() != null) {
                long recentHours = java.time.Duration.between(post.getPublishedAt(), LocalDateTime.now()).toHours();
                if (recentHours <= 24) {
                    recencyBonus = 12;
                } else if (recentHours <= 72) {
                    recencyBonus = 6;
                }
            }
            scoreMap.merge(post.getId(), baseScore + popularityScore + recencyBonus, Integer::sum);
            reasonMap.computeIfAbsent(post.getId(), key -> new ArrayList<>());
            if (!reasonMap.get(post.getId()).contains(reason)) {
                reasonMap.get(post.getId()).add(reason);
            }
        }
    }

    private List<Long> loadRecentHistoryPostIds(Long currentUserId, int limit) {
        return loadActivePostIds(userBrowseHistoryMapper.selectList(new LambdaQueryWrapper<UserBrowseHistory>()
                        .eq(UserBrowseHistory::getUserId, currentUserId)
                        .orderByDesc(UserBrowseHistory::getLastViewedAt)
                        .last("LIMIT " + limit))
                .stream()
                .map(UserBrowseHistory::getPostId)
                .toList());
    }

    private Set<Long> loadInterestTagIds(Long currentUserId, List<Long> historyPostIds) {
        Set<Long> sourcePostIds = new LinkedHashSet<>(historyPostIds);
        sourcePostIds.addAll(loadActivePostIds(postLikeMapper.selectList(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUserId, currentUserId)
                .eq(PostLike::getStatus, "ACTIVE")
                .orderByDesc(PostLike::getCreatedAt)
                .last("LIMIT 12")).stream().map(PostLike::getPostId).toList()));
        sourcePostIds.addAll(loadActivePostIds(postFavoriteMapper.selectList(new LambdaQueryWrapper<PostFavorite>()
                .eq(PostFavorite::getUserId, currentUserId)
                .eq(PostFavorite::getStatus, "ACTIVE")
                .orderByDesc(PostFavorite::getCreatedAt)
                .last("LIMIT 12")).stream().map(PostFavorite::getPostId).toList()));
        if (sourcePostIds.isEmpty()) {
            return Collections.emptySet();
        }
        return postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRel>()
                        .in(PostTagRel::getPostId, sourcePostIds))
                .stream()
                .map(PostTagRel::getTagId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> loadFollowedAuthorIds(Long currentUserId) {
        return userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFollowerId, currentUserId)
                        .eq(UserFollow::getStatus, "ACTIVE")
                        .orderByDesc(UserFollow::getFollowedAt)
                        .last("LIMIT 20"))
                .stream()
                .map(UserFollow::getFolloweeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<Long> loadActivePostIds(List<Long> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyList();
        }
        return postMapper.selectList(new LambdaQueryWrapper<Post>()
                        .in(Post::getId, postIds)
                        .eq(Post::getIsDeleted, 0)
                        .eq(Post::getStatus, "PUBLISHED"))
                .stream()
                .map(Post::getId)
                .toList();
    }

    private List<Post> loadLatestPostsByAuthors(Set<Long> authorIds, Long currentUserId, int limit) {
        if (CollectionUtils.isEmpty(authorIds)) {
            return Collections.emptyList();
        }
        return postMapper.selectList(new LambdaQueryWrapper<Post>()
                .in(Post::getAuthorId, authorIds)
                .ne(currentUserId != null, Post::getAuthorId, currentUserId)
                .eq(Post::getIsDeleted, 0)
                .eq(Post::getStatus, "PUBLISHED")
                .orderByDesc(Post::getPublishedAt)
                .last("LIMIT " + limit));
    }

    private List<Post> loadPostsByTagIds(Set<Long> tagIds, Long currentUserId, int limit) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return Collections.emptyList();
        }
        List<Long> postIds = postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRel>()
                        .in(PostTagRel::getTagId, tagIds))
                .stream()
                .map(PostTagRel::getPostId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (postIds.isEmpty()) {
            return Collections.emptyList();
        }
        return postMapper.selectList(new LambdaQueryWrapper<Post>()
                .in(Post::getId, postIds)
                .ne(currentUserId != null, Post::getAuthorId, currentUserId)
                .eq(Post::getIsDeleted, 0)
                .eq(Post::getStatus, "PUBLISHED")
                .orderByDesc(Post::getPublishedAt)
                .last("LIMIT " + limit));
    }

    private List<Post> loadTopHotPosts(int limit) {
        return postMapper.selectList(new LambdaQueryWrapper<Post>()
                .eq(Post::getIsDeleted, 0)
                .eq(Post::getStatus, "PUBLISHED")
                .orderByDesc(Post::getLikeCount)
                .orderByDesc(Post::getFavoriteCount)
                .orderByDesc(Post::getCommentCount)
                .orderByDesc(Post::getViewCount)
                .orderByDesc(Post::getPublishedAt)
                .last("LIMIT " + limit));
    }

    private List<Post> loadPublishedPostsByIds(List<Long> postIds) {
        if (CollectionUtils.isEmpty(postIds)) {
            return Collections.emptyList();
        }
        Map<Long, Integer> orderMap = new HashMap<>();
        for (int index = 0; index < postIds.size(); index++) {
            orderMap.put(postIds.get(index), index);
        }
        return postMapper.selectList(new LambdaQueryWrapper<Post>()
                        .in(Post::getId, postIds)
                        .eq(Post::getIsDeleted, 0)
                        .eq(Post::getStatus, "PUBLISHED"))
                .stream()
                .sorted(Comparator.comparingInt(post -> orderMap.getOrDefault(post.getId(), Integer.MAX_VALUE)))
                .toList();
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
     * 校验并同步帖子标签关系：
     * 1) 新增关联时 post_count +1
     * 2) 移除关联时 post_count -1
     */
    private void syncPostTags(Long postId, List<Long> inputTagIds) {
        LinkedHashSet<Long> nextTagIds = normalizeIds(inputTagIds);
        validateTagIds(nextTagIds);

        // 注意：同步标签关系常见做法是算出“待新增集合”和“待移除集合”，比先删后插更稳，也更利于维护计数。
        List<PostTagRel> existingRels = postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRel>()
                .eq(PostTagRel::getPostId, postId));
        Set<Long> existingTagIds = existingRels.stream()
                .map(PostTagRel::getTagId)
                .collect(Collectors.toCollection(HashSet::new));

        Set<Long> toAdd = new HashSet<>(nextTagIds);
        toAdd.removeAll(existingTagIds);

        Set<Long> toRemove = new HashSet<>(existingTagIds);
        toRemove.removeAll(nextTagIds);

        if (!toAdd.isEmpty()) {
            for (Long tagId : toAdd) {
                postTagRelMapper.insert(PostTagRel.builder()
                        .postId(postId)
                        .tagId(tagId)
                        .build());
                tagMapper.update(null, new LambdaUpdateWrapper<Tag>()
                        .eq(Tag::getId, tagId)
                        .setSql("post_count = post_count + 1"));
            }
        }
        if (!toRemove.isEmpty()) {
            postTagRelMapper.delete(new LambdaQueryWrapper<PostTagRel>()
                    .eq(PostTagRel::getPostId, postId)
                    .in(PostTagRel::getTagId, toRemove));
            for (Long tagId : toRemove) {
                tagMapper.update(null, new LambdaUpdateWrapper<Tag>()
                        .eq(Tag::getId, tagId)
                        .setSql("post_count = IF(post_count > 0, post_count - 1, 0)"));
            }
        }
    }

    /**
     * 同步帖子附件绑定关系：只允许绑定当前用户上传的文件。
     */
    private void syncPostAttachments(Long currentUserId, Long postId, List<Long> inputFileIds) {
        LinkedHashSet<Long> nextFileIds = normalizeIds(inputFileIds);

        // 注意：附件同步与标签同步思路类似，都是先求差集，再执行解绑与绑定。
        List<FileObject> existingFiles = fileObjectMapper.selectList(new LambdaQueryWrapper<FileObject>()
                .eq(FileObject::getBizType, "POST")
                .eq(FileObject::getBizId, postId)
                .eq(FileObject::getStatus, "BOUND")
                .eq(FileObject::getUploaderId, currentUserId));
        Set<Long> existingFileIds = existingFiles.stream()
                .map(FileObject::getId)
                .collect(Collectors.toSet());

        Set<Long> toUnbind = new HashSet<>(existingFileIds);
        toUnbind.removeAll(nextFileIds);
        if (!toUnbind.isEmpty()) {
            fileObjectMapper.update(null, new LambdaUpdateWrapper<FileObject>()
                    .in(FileObject::getId, toUnbind)
                    .eq(FileObject::getUploaderId, currentUserId)
                    .set(FileObject::getBizType, null)
                    .set(FileObject::getBizId, null)
                    .set(FileObject::getStatus, "UPLOADED"));
        }

        if (!nextFileIds.isEmpty()) {
            // 注意：先批量绑定，再逐个回写 sort_order，这样前端拖拽排序结果就能在详情页稳定复现。
            fileObjectMapper.update(null, new LambdaUpdateWrapper<FileObject>()
                    .in(FileObject::getId, nextFileIds)
                    .eq(FileObject::getUploaderId, currentUserId)
                    .set(FileObject::getBizType, "POST")
                    .set(FileObject::getBizId, postId)
                    .set(FileObject::getStatus, "BOUND"));

            int order = 0;
            for (Long fileId : nextFileIds) {
                fileObjectMapper.update(null, new LambdaUpdateWrapper<FileObject>()
                        .eq(FileObject::getId, fileId)
                        .eq(FileObject::getUploaderId, currentUserId)
                        .set(FileObject::getSortOrder, order++));
            }
        }
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
     * 校验帖子存在且当前用户为作者（用于编辑/删除/下架）。
     */
    private Post mustGetOwnedPost(Long currentUserId, Long postId) {
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>()
                .eq(Post::getId, postId)
                .eq(Post::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (post == null) {
            throw BizException.of(ErrorCode.POST_NOT_FOUND);
        }
        if (!Objects.equals(post.getAuthorId(), currentUserId)) {
            throw BizException.of(ErrorCode.AUTH_ACCESS_DENIED);
        }
        return post;
    }

    /**
     * 校验标签合法性：全部必须存在且状态为 ACTIVE。
     */
    private void validateTagIds(Set<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        long activeTagCount = tagMapper.selectCount(new LambdaQueryWrapper<Tag>()
                .in(Tag::getId, tagIds)
                .eq(Tag::getStatus, "ACTIVE"));
        if (activeTagCount != tagIds.size()) {
            throw BizException.of(ErrorCode.POST_TAG_INVALID);
        }
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
        /* 构建用户摘要映射
         合并函数 — 当遇到重复 Key 时，保留第一个值（避免抛出异常）
         LinkedHashMap::new	使用 LinkedHashMap 保持插入顺序
         Function.identity() 等价 user -> user
        */
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
            // 如果帖子列表为空，直接返回空映射。
            return Collections.emptyMap();
        }
        List<PostTagRel> rels = postTagRelMapper.selectList(new LambdaQueryWrapper<PostTagRel>()
                .in(PostTagRel::getPostId, postIds));
        if (rels.isEmpty()) {
            // 如果帖子没有关联标签，直接返回空映射。
            return Collections.emptyMap();
        }
        // 提取所有关联的标签 ID 列表
        List<Long> tagIds = rels.stream().map(PostTagRel::getTagId).distinct().toList();
        // 批量加载标签信息
        Map<Long, TagVo> tagVoMap = tagMapper.selectList(new LambdaQueryWrapper<Tag>().in(Tag::getId, tagIds)).stream()
                .collect(Collectors.toMap(Tag::getId, tag -> {
                    TagVo vo = new TagVo();
                    vo.setId(tag.getId());
                    vo.setName(tag.getName());
                    return vo;
                }, (a, b) -> a)); //(a, b) -> a	合并函数 — 当遇到重复 Key 时，保留第一个值（避免抛出异常）

        // 构建帖子-标签映射
        Map<Long, List<TagVo>> result = new LinkedHashMap<>();
        for (PostTagRel rel : rels) {
            TagVo tagVo = tagVoMap.get(rel.getTagId());
            if (tagVo == null) {
                // 如果该帖子没有关联的标签，跳过。
                continue;
            }
            /*
                如果 postId 已存在 → 直接返回已有的 List<TagVo>
                如果 postId 不存在 → 创建新的 ArrayList<>() 并存入
                然后将当前标签 add 到列表中
             */
            result.computeIfAbsent(rel.getPostId(), k -> new ArrayList<>()).add(tagVo);
        }
        return result;
    }

    /**
     * 加载当前用户点赞过的帖子集合。
     */
    private Set<Long> loadLikedPostSet(Long currentUserId, List<Long> postIds) {
        if (currentUserId == null || CollectionUtils.isEmpty(postIds)) {
            // 未登录情况 或 帖子列表为空，直接返回空集合。
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
            // 未登录情况 或 帖子列表为空，直接返回空集合。
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
     * 加载帖子附件文件信息，供前端编辑和预览。
     */
    private List<FileObjectVo> loadAttachmentFiles(Long postId) {
        return fileObjectMapper.selectList(new LambdaQueryWrapper<FileObject>()
                        .eq(FileObject::getBizType, "POST")
                        .eq(FileObject::getBizId, postId)
                        .eq(FileObject::getStatus, "BOUND")
                        .orderByAsc(FileObject::getSortOrder))
                .stream()
                .map(file -> {
                    FileObjectVo vo = new FileObjectVo();
                    vo.setId(file.getId());
                    vo.setAccessUrl(file.getAccessUrl());
                    vo.setOriginalName(file.getOriginalName());
                    vo.setMimeType(file.getMimeType());
                    vo.setSizeBytes(file.getSizeBytes());
                    vo.setBizType(file.getBizType());
                    vo.setBizId(file.getBizId());
                    return vo;
                })
                .toList();
    }

    /**
     * 帖子点赞通知：发送给帖主。
     */
    private void createPostLikeNotification(Long actorId, Post post) {
        // 点赞帖子时通知作者，形成作者侧的互动反馈。
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

    private void createFollowersPostNotifications(Post post) {
        List<Long> followerIds = userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFolloweeId, post.getAuthorId())
                        .eq(UserFollow::getStatus, "ACTIVE"))
                .stream()
                .map(UserFollow::getFollowerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (followerIds.isEmpty()) {
            return;
        }
        for (Long followerId : followerIds) {
            notificationService.create(
                    followerId,
                    post.getAuthorId(),
                    NotificationType.FOLLOW,
                    NotificationTargetType.POST,
                    post.getId(),
                    "你关注的用户发布了新帖子",
                    "你关注的作者发布了《" + post.getTitle() + "》"
            );
        }
    }
}
