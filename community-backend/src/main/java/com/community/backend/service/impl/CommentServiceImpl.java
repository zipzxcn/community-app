package com.community.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.backend.common.api.PageResponse;
import com.community.backend.common.constant.NotificationTargetType;
import com.community.backend.common.constant.NotificationType;
import com.community.backend.common.error.BizException;
import com.community.backend.common.error.ErrorCode;
import com.community.backend.dto.comment.CreateCommentRequest;
import com.community.backend.dto.comment.ReplyCommentRequest;
import com.community.backend.entity.AppUser;
import com.community.backend.entity.CommentLike;
import com.community.backend.entity.Post;
import com.community.backend.entity.PostComment;
import com.community.backend.mapper.AppUserMapper;
import com.community.backend.mapper.CommentLikeMapper;
import com.community.backend.mapper.PostCommentMapper;
import com.community.backend.mapper.PostMapper;
import com.community.backend.service.CommentService;
import com.community.backend.service.NotificationService;
import com.community.backend.vo.comment.CommentItemVo;
import com.community.backend.vo.user.UserSummaryVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 评论服务实现：评论树查询、评论/回复、删除与评论点赞。
 */
/**
 * 评论服务实现：
 * - 支持根评论与回复两级/多级关系。
 * - 负责同步评论数、回复数、点赞状态和通知触达。
 */
@Service
public class CommentServiceImpl implements CommentService {

    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final AppUserMapper appUserMapper;
    private final NotificationService notificationService;

    public CommentServiceImpl(PostMapper postMapper,
                              PostCommentMapper postCommentMapper,
                              CommentLikeMapper commentLikeMapper,
                              AppUserMapper appUserMapper,
                              NotificationService notificationService) {
        this.postMapper = postMapper;
        this.postCommentMapper = postCommentMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.appUserMapper = appUserMapper;
        this.notificationService = notificationService;
    }

    /**
     * 发表评论：
     * - 校验帖子可评论
     * - 创建根评论
     * - 维护帖子评论计数并触发通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentItemVo createComment(Long currentUserId, Long postId, CreateCommentRequest request) {
        Post post = mustGetCommentEnabledPost(postId);
        // 先创建评论主记录，再回写帖子评论计数，保持统计值与明细表一致。
        PostComment comment = PostComment.builder()
                .postId(post.getId())
                .userId(currentUserId)
                .parentId(0L)
                .rootId(0L)
                .content(request.getContent())
                .status("NORMAL")
                .likeCount(0)
                .replyCount(0)
                .build();
        postCommentMapper.insert(comment);
        increasePostCommentCount(postId);
        // 评论帖子后通知帖子作者
        // 评论/回复会触达被评论者或帖主，增强社区互动反馈闭环。
        notificationService.create(
                post.getAuthorId(),
                currentUserId,
                NotificationType.COMMENT,
                NotificationTargetType.POST,
                post.getId(),
                "你的帖子有新评论",
                "有人评论了你的帖子"
        );
        return toCommentVo(comment, currentUserId, Collections.emptyMap(), Collections.emptySet());
    }

    /**
     * 回复评论：
     * - 校验 parentId
     * - 创建回复并回写 reply_count
     * - 通知被回复人/帖主
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentItemVo reply(Long currentUserId, Long commentId, ReplyCommentRequest request) {
        if (!commentId.equals(request.getParentId())) {
            throw BizException.of(ErrorCode.COMMENT_PARENT_MISMATCH);
        }
        PostComment parent = mustGetActiveComment(commentId);
        Post post = mustGetCommentEnabledPost(parent.getPostId());

        Long rootId = parent.getRootId() == null || parent.getRootId() == 0 ? parent.getId() : parent.getRootId();
        PostComment reply = PostComment.builder()
                .postId(parent.getPostId())
                .userId(currentUserId)
                .parentId(parent.getId())
                .rootId(rootId)
                .replyToUserId(parent.getUserId())
                .content(request.getContent())
                .status("NORMAL")
                .likeCount(0)
                .replyCount(0)
                .build();
        postCommentMapper.insert(reply);

        postCommentMapper.update(null, new LambdaUpdateWrapper<PostComment>()
                .eq(PostComment::getId, parent.getId())
                .setSql("reply_count = reply_count + 1"));
        increasePostCommentCount(post.getId());

        // 回复评论后通知被回复用户
        notificationService.create(
                parent.getUserId(),
                currentUserId,
                NotificationType.COMMENT,
                NotificationTargetType.COMMENT,
                parent.getId(),
                "你收到新的回复",
                "有人回复了你的评论"
        );
        // 如果帖子作者不是被回复用户，也补一条帖子维度通知
        if (!post.getAuthorId().equals(parent.getUserId())) {
            notificationService.create(
                    post.getAuthorId(),
                    currentUserId,
                    NotificationType.COMMENT,
                    NotificationTargetType.POST,
                    post.getId(),
                    "你的帖子有新回复",
                    "有人在你的帖子下发布了回复"
            );
        }

        Map<Long, UserSummaryVo> userMap = loadUserMap(List.of(currentUserId, parent.getUserId()));
        return toCommentVo(reply, currentUserId, userMap, Collections.emptySet());
    }

    /**
     * 删除评论：评论作者或帖主可删，并同步修正相关计数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long currentUserId, Long commentId) {
        PostComment comment = postCommentMapper.selectById(commentId);
        if (comment == null || !"NORMAL".equals(comment.getStatus())) {
            return;
        }
        Post post = postMapper.selectById(comment.getPostId());
        if (post == null || post.getIsDeleted() == 1) {
            return;
        }
        boolean isCommentOwner = currentUserId.equals(comment.getUserId());
        boolean isPostOwner = currentUserId.equals(post.getAuthorId());
        if (!isCommentOwner && !isPostOwner) {
            throw BizException.of(ErrorCode.COMMENT_DELETE_FORBIDDEN);
        }

        String status = isCommentOwner ? "DELETED_BY_AUTHOR" : "DELETED_BY_POST_OWNER";
        postCommentMapper.update(null, new LambdaUpdateWrapper<PostComment>()
                .eq(PostComment::getId, commentId)
                .eq(PostComment::getStatus, "NORMAL")
                .set(PostComment::getStatus, status)
                .set(PostComment::getDeletedAt, LocalDateTime.now()));

        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, comment.getPostId())
                .setSql("comment_count = IF(comment_count > 0, comment_count - 1, 0)"));

        if (comment.getParentId() != null && comment.getParentId() > 0) {
            postCommentMapper.update(null, new LambdaUpdateWrapper<PostComment>()
                    .eq(PostComment::getId, comment.getParentId())
                    .setSql("reply_count = IF(reply_count > 0, reply_count - 1, 0)"));
        }
    }

    /**
     * 点赞评论：支持幂等与历史记录恢复。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long currentUserId, Long commentId) {
        mustGetActiveComment(commentId);
        CommentLike existing = commentLikeMapper.selectOne(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getUserId, currentUserId)
                .eq(CommentLike::getCommentId, commentId)
                .last("LIMIT 1"));
        if (existing == null) {
            commentLikeMapper.insert(CommentLike.builder()
                    .userId(currentUserId)
                    .commentId(commentId)
                    .status("ACTIVE")
                    .build());
            increaseCommentLikeCount(commentId);
            return;
        }
        if ("ACTIVE".equals(existing.getStatus())) {
            return;
        }
        commentLikeMapper.update(null, new LambdaUpdateWrapper<CommentLike>()
                .eq(CommentLike::getId, existing.getId())
                .set(CommentLike::getStatus, "ACTIVE"));
        increaseCommentLikeCount(commentId);
    }

    /**
     * 取消评论点赞。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(Long currentUserId, Long commentId) {
        CommentLike existing = commentLikeMapper.selectOne(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getUserId, currentUserId)
                .eq(CommentLike::getCommentId, commentId)
                .last("LIMIT 1"));
        if (existing == null || !"ACTIVE".equals(existing.getStatus())) {
            return;
        }
        commentLikeMapper.update(null, new LambdaUpdateWrapper<CommentLike>()
                .eq(CommentLike::getId, existing.getId())
                .set(CommentLike::getStatus, "CANCELLED"));
        postCommentMapper.update(null, new LambdaUpdateWrapper<PostComment>()
                .eq(PostComment::getId, commentId)
                .setSql("like_count = IF(like_count > 0, like_count - 1, 0)"));
    }

    /**
     * 查询帖子评论树（根评论 + 回复列表）。
     */
    @Override
    public PageResponse<CommentItemVo> listByPost(Long postId, Long currentUserId, Long page, Long size) {
        mustGetVisiblePost(postId);

        Page<PostComment> pager = new Page<>(page, size);
        Page<PostComment> roots = postCommentMapper.selectPage(pager, new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .eq(PostComment::getParentId, 0L)
                .eq(PostComment::getStatus, "NORMAL")
                .orderByDesc(PostComment::getCreatedAt));

        if (roots.getRecords().isEmpty()) {
            return PageResponse.<CommentItemVo>builder()
                    .list(Collections.emptyList())
                    .page(roots.getCurrent())
                    .size(roots.getSize())
                    .total(roots.getTotal())
                    .hasMore(Boolean.FALSE)
                    .build();
        }

        List<Long> rootIds = roots.getRecords().stream().map(PostComment::getId).toList();
        List<PostComment> replies = postCommentMapper.selectList(new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .in(PostComment::getRootId, rootIds)
                .ne(PostComment::getParentId, 0L)
                .eq(PostComment::getStatus, "NORMAL")
                .orderByAsc(PostComment::getCreatedAt));

        List<PostComment> all = new ArrayList<>(roots.getRecords());
        all.addAll(replies);
        Map<Long, UserSummaryVo> userMap = loadUserMap(all.stream()
                .flatMap(comment -> java.util.stream.Stream.of(comment.getUserId(), comment.getReplyToUserId()))
                .filter(id -> id != null && id > 0)
                .toList());
        Set<Long> likedSet = loadLikedCommentSet(currentUserId, all.stream().map(PostComment::getId).toList());

        Map<Long, List<CommentItemVo>> replyMap = new LinkedHashMap<>();
        for (PostComment reply : replies) {
            CommentItemVo replyVo = toCommentVo(reply, currentUserId, userMap, likedSet);
            replyMap.computeIfAbsent(reply.getRootId(), k -> new ArrayList<>()).add(replyVo);
        }

        List<CommentItemVo> list = roots.getRecords().stream().map(root -> {
            CommentItemVo rootVo = toCommentVo(root, currentUserId, userMap, likedSet);
            rootVo.setReplies(replyMap.getOrDefault(root.getId(), Collections.emptyList()));
            return rootVo;
        }).toList();

        return PageResponse.<CommentItemVo>builder()
                .list(list)
                .page(roots.getCurrent())
                .size(roots.getSize())
                .total(roots.getTotal())
                .hasMore(roots.getCurrent() * roots.getSize() < roots.getTotal())
                .build();
    }

    /**
     * 校验帖子可见且允许评论。
     */
    private Post mustGetCommentEnabledPost(Long postId) {
        // 评论之前先确认帖子可见且允许评论，避免对隐藏/关闭评论的帖子继续写入。
        Post post = mustGetVisiblePost(postId);
        if (post.getAllowComment() == null || post.getAllowComment() == 0) {
            throw BizException.of(ErrorCode.COMMENT_CLOSED);
        }
        return post;
    }

    /**
     * 校验帖子可见。
     */
    private Post mustGetVisiblePost(Long postId) {
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
     * 校验评论是否存在且为 NORMAL 状态。
     */
    private PostComment mustGetActiveComment(Long commentId) {
        PostComment comment = postCommentMapper.selectOne(new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getId, commentId)
                .eq(PostComment::getStatus, "NORMAL")
                .last("LIMIT 1"));
        if (comment == null) {
            throw BizException.of(ErrorCode.COMMENT_NOT_FOUND);
        }
        return comment;
    }

    /**
     * 帖子评论数 +1。
     */
    private void increasePostCommentCount(Long postId) {
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .eq(Post::getId, postId)
                .setSql("comment_count = comment_count + 1"));
    }

    /**
     * 评论点赞数 +1。
     */
    private void increaseCommentLikeCount(Long commentId) {
        postCommentMapper.update(null, new LambdaUpdateWrapper<PostComment>()
                .eq(PostComment::getId, commentId)
                .setSql("like_count = like_count + 1"));
    }

    /**
     * 加载当前用户点赞过的评论集合。
     */
    private Set<Long> loadLikedCommentSet(Long currentUserId, List<Long> commentIds) {
        if (currentUserId == null || commentIds.isEmpty()) {
            return Collections.emptySet();
        }
        return commentLikeMapper.selectList(new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getUserId, currentUserId)
                        .eq(CommentLike::getStatus, "ACTIVE")
                        .in(CommentLike::getCommentId, commentIds))
                .stream()
                .map(CommentLike::getCommentId)
                .collect(Collectors.toSet());
    }

    /**
     * 批量加载评论相关用户摘要信息。
     */
    private Map<Long, UserSummaryVo> loadUserMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return appUserMapper.selectList(new LambdaQueryWrapper<AppUser>()
                        .in(AppUser::getId, userIds)
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
     * 评论实体转 VO。
     */
    private CommentItemVo toCommentVo(PostComment comment,
                                      Long currentUserId,
                                      Map<Long, UserSummaryVo> userMap,
                                      Set<Long> likedSet) {
        CommentItemVo vo = new CommentItemVo();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setParentId(comment.getParentId());
        vo.setRootId(comment.getRootId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setReplyCount(comment.getReplyCount());
        vo.setStatus(comment.getStatus());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setUser(userMap.get(comment.getUserId()));
        vo.setReplyToUser(userMap.get(comment.getReplyToUserId()));
        vo.setReplies(Collections.emptyList());
        vo.setLiked(currentUserId != null && likedSet.contains(comment.getId()));
        return vo;
    }
}
