package com.community.backend.controller;

import com.community.backend.common.api.ApiResponse;
import com.community.backend.common.api.PageResponse;
import com.community.backend.security.SecurityUtils;
import com.community.backend.service.FollowService;
import com.community.backend.vo.follow.FollowUserVo;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 关注模块控制器：关注/取关与关注关系列表查询。
 */
@Validated
@RestController
@RequestMapping("/api/v1/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    /**
     * 关注目标用户。
     */
    @PostMapping("/{targetUserId}")
    public ApiResponse<Void> follow(@PathVariable Long targetUserId) {
        Long currentUserId = SecurityUtils.requireUserId();
        followService.follow(currentUserId, targetUserId);
        return ApiResponse.success(null);
    }

    /**
     * 取关目标用户。
     */
    @DeleteMapping("/{targetUserId}")
    public ApiResponse<Void> unfollow(@PathVariable Long targetUserId) {
        Long currentUserId = SecurityUtils.requireUserId();
        followService.unfollow(currentUserId, targetUserId);
        return ApiResponse.success(null);
    }

    /**
     * 我关注的人列表。
     */
    @GetMapping("/following")
    public ApiResponse<PageResponse<FollowUserVo>> following(
            @RequestParam(defaultValue = "1") @Min(1) Long page,
            @RequestParam(defaultValue = "20") @Min(1) Long size) {
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(followService.listFollowing(currentUserId, page, size));
    }

    /**
     * 我的粉丝列表。
     */
    @GetMapping("/followers")
    public ApiResponse<PageResponse<FollowUserVo>> followers(
            @RequestParam(defaultValue = "1") @Min(1) Long page,
            @RequestParam(defaultValue = "20") @Min(1) Long size) {
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(followService.listFollowers(currentUserId, page, size));
    }

    /**
     * 我的互关列表。
     */
    @GetMapping("/mutual")
    public ApiResponse<PageResponse<FollowUserVo>> mutual(
            @RequestParam(defaultValue = "1") @Min(1) Long page,
            @RequestParam(defaultValue = "20") @Min(1) Long size) {
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(followService.listMutual(currentUserId, page, size));
    }
}
