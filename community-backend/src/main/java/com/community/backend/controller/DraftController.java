package com.community.backend.controller;

import com.community.backend.common.api.ApiResponse;
import com.community.backend.common.api.PageResponse;
import com.community.backend.dto.draft.SaveDraftRequest;
import com.community.backend.security.SecurityUtils;
import com.community.backend.service.DraftService;
import com.community.backend.vo.draft.DraftDetailVo;
import com.community.backend.vo.draft.DraftItemVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 草稿模块控制器：草稿查询、保存、删除与发布。
 */
@Validated
@RestController
@RequestMapping("/api/v1/drafts")
public class DraftController {

    private final DraftService draftService;

    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    /**
     * 草稿列表：返回当前用户的 ACTIVE 草稿。
     */
    @GetMapping
    public ApiResponse<PageResponse<DraftItemVo>> list(
            @RequestParam(defaultValue = "1") @Min(1) Long page,
            @RequestParam(defaultValue = "20") @Min(1) Long size) {
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(draftService.list(currentUserId, page, size));
    }

    /**
     * 草稿详情：仅支持查看自己的草稿。
     */
    @GetMapping("/{draftId}")
    public ApiResponse<DraftDetailVo> detail(@PathVariable Long draftId) {
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(draftService.detail(currentUserId, draftId));
    }

    /**
     * 新建草稿。
     */
    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody SaveDraftRequest request) {
        Long currentUserId = SecurityUtils.requireUserId();
        Long draftId = draftService.create(currentUserId, request);
        return ApiResponse.success(Map.of("draftId", draftId));
    }

    /**
     * 更新草稿：支持自动保存与手动保存。
     */
    @PutMapping("/{draftId}")
    public ApiResponse<DraftDetailVo> update(@PathVariable Long draftId, @Valid @RequestBody SaveDraftRequest request) {
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(draftService.update(currentUserId, draftId, request));
    }

    /**
     * 删除草稿：逻辑删除为 DISCARDED。
     */
    @DeleteMapping("/{draftId}")
    public ApiResponse<Void> delete(@PathVariable Long draftId) {
        Long currentUserId = SecurityUtils.requireUserId();
        draftService.delete(currentUserId, draftId);
        return ApiResponse.success(null);
    }

    /**
     * 草稿发布：发布后返回 postId。
     */
    @PostMapping("/{draftId}/publish")
    public ApiResponse<Map<String, Long>> publish(@PathVariable Long draftId) {
        Long currentUserId = SecurityUtils.requireUserId();
        Long postId = draftService.publish(currentUserId, draftId);
        return ApiResponse.success(Map.of("postId", postId));
    }
}
