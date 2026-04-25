package com.community.backend.controller;

import com.community.backend.common.api.ApiResponse;
import com.community.backend.common.api.PageResponse;
import com.community.backend.dto.user.UpdateProfileRequest;
import com.community.backend.dto.user.UserSearchQuery;
import com.community.backend.security.SecurityUtils;
import com.community.backend.service.UserService;
import com.community.backend.vo.post.PostListItemVo;
import com.community.backend.vo.user.UserProfileVo;
import com.community.backend.vo.user.UserSummaryVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户模块控制器：主页查询、用户搜索与个人资料编辑。
 */
@RestController
@Validated
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户主页：支持游客访问，登录态下补充关注关系信息。
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserProfileVo> profile(@PathVariable Long userId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(userService.getProfile(userId, currentUserId));
    }

    /**
     * 用户搜索：按用户名/昵称关键词分页检索。
     */
    @GetMapping("/search")
    public ApiResponse<PageResponse<UserSummaryVo>> search(@Valid UserSearchQuery query) {
        return ApiResponse.success(userService.search(query));
    }

    /**
     * 修改我的资料：允许更新昵称、头像与简介。
     */
    @PutMapping("/me/profile")
    public ApiResponse<UserProfileVo> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(userService.updateProfile(currentUserId, request));
    }

    /**
     * 用户已发布帖子列表：支持游客查询。
     */
    @GetMapping("/{userId}/posts")
    public ApiResponse<PageResponse<PostListItemVo>> posts(@PathVariable Long userId,
                                                           @RequestParam(defaultValue = "1") @Min(1) Long page,
                                                           @RequestParam(defaultValue = "20") @Min(1) Long size) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(userService.listUserPosts(userId, currentUserId, page, size));
    }

    /**
     * 用户收藏帖子列表：仅本人可查。
     */
    @GetMapping("/{userId}/favorites")
    public ApiResponse<PageResponse<PostListItemVo>> favorites(@PathVariable Long userId,
                                                               @RequestParam(defaultValue = "1") @Min(1) Long page,
                                                               @RequestParam(defaultValue = "20") @Min(1) Long size) {
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(userService.listUserFavorites(userId, currentUserId, page, size));
    }

    /**
     * 用户点赞帖子列表：仅本人可查。
     */
    @GetMapping("/{userId}/likes")
    public ApiResponse<PageResponse<PostListItemVo>> likes(@PathVariable Long userId,
                                                           @RequestParam(defaultValue = "1") @Min(1) Long page,
                                                           @RequestParam(defaultValue = "20") @Min(1) Long size) {
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(userService.listUserLikes(userId, currentUserId, page, size));
    }
}
