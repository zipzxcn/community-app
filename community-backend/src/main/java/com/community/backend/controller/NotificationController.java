package com.community.backend.controller;

import com.community.backend.common.api.ApiResponse;
import com.community.backend.common.api.PageResponse;
import com.community.backend.dto.notification.ReadAllNotificationRequest;
import com.community.backend.security.SecurityUtils;
import com.community.backend.service.NotificationService;
import com.community.backend.vo.notification.NotificationItemVo;
import com.community.backend.vo.notification.NotificationUnreadVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知中心控制器：通知列表、未读统计与已读状态维护。
 */
@Validated
@Tag(name = "Notification", description = "通知中心接口")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 查询通知列表：支持类型、目标类型、已读状态和关键词筛选。
     */
    @Operation(summary = "通知列表", description = "支持按 type/targetType/isRead/keyword 筛选")
    @GetMapping
    public ApiResponse<PageResponse<NotificationItemVo>> list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) Long page,
            @RequestParam(defaultValue = "20") @Min(1) Long size) {
        // 查询通知列表，支持按类型和已读状态筛选
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(notificationService.list(currentUserId, type, targetType, isRead, keyword, page, size));
    }

    /**
     * 获取未读汇总：返回总数及各类型未读数。
     */
    @Operation(summary = "未读计数", description = "返回总未读数与分类未读数")
    @GetMapping("/unread-count")
    public ApiResponse<NotificationUnreadVo> unreadCount() {
        // 聚合未读数，前端可直接用于红点展示
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(notificationService.unreadCount(currentUserId));
    }

    /**
     * 标记单条通知为已读。
     */
    @Operation(summary = "单条已读", description = "将指定通知标记为已读")
    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markRead(@PathVariable Long notificationId) {
        // 标记单条通知为已读
        Long currentUserId = SecurityUtils.requireUserId();
        notificationService.markRead(currentUserId, notificationId);
        return ApiResponse.success(null);
    }

    /**
     * 批量标记已读：可全量或按 type 过滤。
     */
    @Operation(summary = "批量已读", description = "type 为空时全部已读，传 type 时按类型批量已读")
    @PatchMapping("/read-all")
    public ApiResponse<Void> readAll(@Valid @RequestBody(required = false) ReadAllNotificationRequest request) {
        // 批量已读；type 为空时表示全部已读
        Long currentUserId = SecurityUtils.requireUserId();
        String type = request == null ? null : request.getType();
        notificationService.markAllRead(currentUserId, type);
        return ApiResponse.success(null);
    }
}
