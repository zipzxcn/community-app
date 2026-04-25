package com.community.backend.controller;

import com.community.backend.common.api.ApiResponse;
import com.community.backend.common.api.PageResponse;
import com.community.backend.dto.comment.CreateCommentRequest;
import com.community.backend.dto.comment.ReplyCommentRequest;
import com.community.backend.security.SecurityUtils;
import com.community.backend.service.CommentService;
import com.community.backend.vo.comment.CommentItemVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论模块控制器：评论、回复、删除与点赞操作。
 */
@Validated
@RestController
@RequestMapping("/api/v1")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 按帖子分页查询评论树（根评论 + 回复）。
     */
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<PageResponse<CommentItemVo>> listByPost(@PathVariable Long postId,
                                                               @RequestParam(defaultValue = "1") @Min(1) Long page,
                                                               @RequestParam(defaultValue = "20") @Min(1) Long size) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(commentService.listByPost(postId, currentUserId, page, size));
    }

    /**
     * 发表评论。
     */
    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<CommentItemVo> createComment(@PathVariable Long postId,
                                                    @Valid @RequestBody CreateCommentRequest request) {
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(commentService.createComment(currentUserId, postId, request));
    }

    /**
     * 回复指定评论。
     */
    @PostMapping("/comments/{commentId}/reply")
    public ApiResponse<CommentItemVo> reply(@PathVariable Long commentId,
                                            @Valid @RequestBody ReplyCommentRequest request) {
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(commentService.reply(currentUserId, commentId, request));
    }

    /**
     * 删除评论（评论作者或帖主可删）。
     */
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId) {
        Long currentUserId = SecurityUtils.requireUserId();
        commentService.deleteComment(currentUserId, commentId);
        return ApiResponse.success(null);
    }

    /**
     * 点赞评论。
     */
    @PostMapping("/comments/{commentId}/like")
    public ApiResponse<Void> like(@PathVariable Long commentId) {
        Long currentUserId = SecurityUtils.requireUserId();
        commentService.like(currentUserId, commentId);
        return ApiResponse.success(null);
    }

    /**
     * 取消点赞评论。
     */
    @DeleteMapping("/comments/{commentId}/like")
    public ApiResponse<Void> unlike(@PathVariable Long commentId) {
        Long currentUserId = SecurityUtils.requireUserId();
        commentService.unlike(currentUserId, commentId);
        return ApiResponse.success(null);
    }
}
