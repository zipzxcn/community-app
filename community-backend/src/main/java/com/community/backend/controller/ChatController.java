package com.community.backend.controller;

import com.community.backend.common.api.ApiResponse;
import com.community.backend.common.api.PageResponse;
import com.community.backend.dto.chat.MarkReadRequest;
import com.community.backend.dto.chat.SendMessageRequest;
import com.community.backend.security.SecurityUtils;
import com.community.backend.service.ChatService;
import com.community.backend.vo.chat.ChatMessageVo;
import com.community.backend.vo.chat.ChatThreadVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 私信控制器：会话管理、消息发送与已读同步。
 */
@Validated
@Tag(name = "Chat", description = "私信聊天接口")
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 会话列表：返回当前用户参与的一对一会话。
     */
    @Operation(summary = "会话列表", description = "分页查询当前用户的一对一会话")
    @GetMapping("/threads")
    public ApiResponse<PageResponse<ChatThreadVo>> threads(
            @RequestParam(defaultValue = "1") @Min(1) Long page,
            @RequestParam(defaultValue = "20") @Min(1) Long size) {
        // 查询当前用户会话列表
        // 先从 SecurityContext 中拿当前用户，再把权限边界交给 service 做会话归属校验。
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(chatService.listThreads(currentUserId, page, size));
    }

    /**
     * 打开会话：若不存在则按规则自动创建。
     */
    @Operation(summary = "打开会话", description = "获取或创建与目标用户的会话")
    @PostMapping("/threads/with/{targetUserId}")
    public ApiResponse<ChatThreadVo> openThread(@PathVariable Long targetUserId) {
        // 获取或创建与目标用户的一对一会话
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(chatService.openThread(currentUserId, targetUserId));
    }

    /**
     * 消息列表：按 cursor 游标向前翻页拉取历史。
     */
    @Operation(summary = "消息列表", description = "按 cursor 拉取会话历史消息")
    @GetMapping("/threads/{threadId}/messages")
    public ApiResponse<PageResponse<ChatMessageVo>> messages(
            @PathVariable Long threadId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") @Min(1) Long size) {
        // 拉取历史消息，cursor 为空时拉取最新一页
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(chatService.listMessages(currentUserId, threadId, cursor, size));
    }

    /**
     * 发送消息：服务层会检查互关关系和会话状态。
     */
    @Operation(summary = "发送消息", description = "发送一条文本或图片消息")
    @PostMapping("/threads/{threadId}/messages")
    public ApiResponse<ChatMessageVo> sendMessage(@PathVariable Long threadId,
                                                  @Valid @RequestBody SendMessageRequest request) {
        // 发送消息，内部会校验互关关系和会话状态
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(chatService.sendMessage(currentUserId, threadId, request));
    }

    /**
     * 标记会话已读：将 lastReadMessageId 及之前消息置为已读。
     */
    @Operation(summary = "标记已读", description = "将某条消息之前的会话消息标记为已读")
    @PostMapping("/threads/{threadId}/read")
    public ApiResponse<Void> markRead(@PathVariable Long threadId, @Valid @RequestBody MarkReadRequest request) {
        // 将某条消息之前的数据全部标记为已读
        Long currentUserId = SecurityUtils.requireUserId();
        chatService.markRead(currentUserId, threadId, request);
        return ApiResponse.success(null);
    }
}
