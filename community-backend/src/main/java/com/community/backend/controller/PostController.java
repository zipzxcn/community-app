package com.community.backend.controller;

import com.community.backend.common.api.ApiResponse;
import com.community.backend.common.api.PageResponse;
import com.community.backend.dto.post.CreatePostRequest;
import com.community.backend.dto.post.PostQueryRequest;
import com.community.backend.security.SecurityUtils;
import com.community.backend.service.PostService;
import com.community.backend.vo.post.PostDetailVo;
import com.community.backend.vo.post.PostListItemVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 帖子模块控制器：发帖、列表、详情、点赞与收藏。
 */
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * 发布帖子：创建后返回 postId。
     */
    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody CreatePostRequest request) {
        Long currentUserId = SecurityUtils.requireUserId();
        Long postId = postService.create(currentUserId, request);
        return ApiResponse.success(Map.of("postId", postId));
    }

    /**
     * 帖子列表：支持关键词、作者、标签与排序筛选。
     */
    @GetMapping
    public ApiResponse<PageResponse<PostListItemVo>> list(@Valid PostQueryRequest query) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(postService.list(query, currentUserId));
    }

    /**
     * 帖子详情：返回正文、作者、标签与当前用户互动状态。
     */
    @GetMapping("/{postId}")
    public ApiResponse<PostDetailVo> detail(@PathVariable Long postId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(postService.detail(postId, currentUserId));
    }

    /**
     * 点赞帖子。
     */
    @PostMapping("/{postId}/like")
    public ApiResponse<Void> like(@PathVariable Long postId) {
        Long currentUserId = SecurityUtils.requireUserId();
        postService.like(currentUserId, postId);
        return ApiResponse.success(null);
    }

    /**
     * 取消点赞帖子。
     */
    @DeleteMapping("/{postId}/like")
    public ApiResponse<Void> unlike(@PathVariable Long postId) {
        Long currentUserId = SecurityUtils.requireUserId();
        postService.unlike(currentUserId, postId);
        return ApiResponse.success(null);
    }

    /**
     * 收藏帖子。
     */
    @PostMapping("/{postId}/favorite")
    public ApiResponse<Void> favorite(@PathVariable Long postId) {
        Long currentUserId = SecurityUtils.requireUserId();
        postService.favorite(currentUserId, postId);
        return ApiResponse.success(null);
    }

    /**
     * 取消收藏帖子。
     */
    @DeleteMapping("/{postId}/favorite")
    public ApiResponse<Void> unfavorite(@PathVariable Long postId) {
        Long currentUserId = SecurityUtils.requireUserId();
        postService.unfavorite(currentUserId, postId);
        return ApiResponse.success(null);
    }
}
