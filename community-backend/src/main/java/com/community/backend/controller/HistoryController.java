package com.community.backend.controller;

import com.community.backend.common.api.ApiResponse;
import com.community.backend.common.api.PageResponse;
import com.community.backend.security.SecurityUtils;
import com.community.backend.service.HistoryService;
import com.community.backend.vo.history.HistoryItemVo;
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
 * 浏览历史控制器：历史记录查询、写入与删除。
 */
@Validated
@RestController
@RequestMapping("/api/v1/histories")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * 历史列表：返回当前用户浏览历史。
     */
    @GetMapping
    public ApiResponse<PageResponse<HistoryItemVo>> list(
            @RequestParam(defaultValue = "1") @Min(1) Long page,
            @RequestParam(defaultValue = "20") @Min(1) Long size) {
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(historyService.list(currentUserId, page, size));
    }

    /**
     * 记录浏览：每次访问帖子时调用一次。
     */
    @PostMapping("/{postId}")
    public ApiResponse<Void> record(@PathVariable Long postId) {
        Long currentUserId = SecurityUtils.requireUserId();
        historyService.record(currentUserId, postId);
        return ApiResponse.success(null);
    }

    /**
     * 删除单条浏览历史。
     */
    @DeleteMapping("/{historyId}")
    public ApiResponse<Void> delete(@PathVariable Long historyId) {
        Long currentUserId = SecurityUtils.requireUserId();
        historyService.delete(currentUserId, historyId);
        return ApiResponse.success(null);
    }

    /**
     * 清空当前用户浏览历史。
     */
    @DeleteMapping
    public ApiResponse<Void> clear() {
        Long currentUserId = SecurityUtils.requireUserId();
        historyService.clear(currentUserId);
        return ApiResponse.success(null);
    }
}
