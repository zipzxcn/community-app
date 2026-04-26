package com.community.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.backend.common.api.PageResponse;
import com.community.backend.common.constant.NotificationTargetType;
import com.community.backend.common.constant.NotificationType;
import com.community.backend.common.error.BizException;
import com.community.backend.common.error.ErrorCode;
import com.community.backend.dto.chat.MarkReadRequest;
import com.community.backend.dto.chat.SendMessageRequest;
import com.community.backend.entity.AppUser;
import com.community.backend.entity.ChatMessage;
import com.community.backend.entity.ChatThread;
import com.community.backend.entity.ChatThreadUser;
import com.community.backend.entity.UserFollow;
import com.community.backend.mapper.AppUserMapper;
import com.community.backend.mapper.ChatMessageMapper;
import com.community.backend.mapper.ChatThreadMapper;
import com.community.backend.mapper.ChatThreadUserMapper;
import com.community.backend.mapper.UserFollowMapper;
import com.community.backend.service.ChatService;
import com.community.backend.service.NotificationService;
import com.community.backend.vo.chat.ChatMessageVo;
import com.community.backend.vo.chat.ChatThreadVo;
import com.community.backend.vo.user.UserSummaryVo;
import com.community.backend.websocket.ChatPushService;
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
 * 聊天服务实现：会话管理、消息收发、已读同步与互关状态约束。
 */
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatThreadMapper chatThreadMapper;
    private final ChatThreadUserMapper chatThreadUserMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserFollowMapper userFollowMapper;
    private final AppUserMapper appUserMapper;
    private final NotificationService notificationService;
    private final ChatPushService chatPushService;

    public ChatServiceImpl(ChatThreadMapper chatThreadMapper,
                           ChatThreadUserMapper chatThreadUserMapper,
                           ChatMessageMapper chatMessageMapper,
                           UserFollowMapper userFollowMapper,
                           AppUserMapper appUserMapper,
                           NotificationService notificationService,
                           ChatPushService chatPushService) {
        this.chatThreadMapper = chatThreadMapper;
        this.chatThreadUserMapper = chatThreadUserMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.userFollowMapper = userFollowMapper;
        this.appUserMapper = appUserMapper;
        this.notificationService = notificationService;
        this.chatPushService = chatPushService;
    }

    /**
     * 查询会话列表：
     * - 仅返回当前用户参与的会话
     * - 动态刷新互关状态导致的 ACTIVE/READ_ONLY
     */
    @Override
    public PageResponse<ChatThreadVo> listThreads(Long currentUserId, Long page, Long size) {
        long safePage = page == null || page < 1 ? 1 : page;
        long safeSize = size == null || size < 1 ? 20 : Math.min(size, 50);

        List<ChatThread> existingThreads = chatThreadMapper.selectList(new LambdaQueryWrapper<ChatThread>()
                .and(w -> w.eq(ChatThread::getUserAId, currentUserId).or().eq(ChatThread::getUserBId, currentUserId))
                .orderByDesc(ChatThread::getLastMessageAt)
                .orderByDesc(ChatThread::getUpdatedAt));
        List<Long> threadIds = existingThreads.stream().map(ChatThread::getId).toList();
        Map<Long, ChatThreadUser> threadUserMap = loadThreadUserMap(currentUserId, threadIds);
        Set<Long> existingPeerIds = existingThreads.stream()
                .map(thread -> getPeerId(thread, currentUserId))
                .collect(Collectors.toSet());
        List<Long> mutualPeerIds = loadMutualOrderedIds(currentUserId);
        Set<Long> allPeerIds = mutualPeerIds.stream().collect(Collectors.toSet());
        allPeerIds.addAll(existingPeerIds);
        Map<Long, UserSummaryVo> peerMap = loadUserSummaryMap(new ArrayList<>(allPeerIds));

        List<ChatThreadVo> list = new ArrayList<>();
        for (ChatThread thread : existingThreads) {
            ChatThread refreshed = refreshThreadStatusByMutual(thread, currentUserId);
            list.add(toThreadVo(currentUserId, refreshed, threadUserMap.get(refreshed.getId()), peerMap.get(getPeerId(refreshed, currentUserId))));
        }

        for (Long peerId : mutualPeerIds) {
            if (existingPeerIds.contains(peerId)) {
                continue;
            }
            ChatThreadVo vo = new ChatThreadVo();
            vo.setThreadId(null);
            vo.setStatus("ACTIVE");
            vo.setUnreadCount(0);
            vo.setPeerUser(peerMap.get(peerId));
            list.add(vo);
        }

        long total = list.size();
        int from = (int) Math.max((safePage - 1) * safeSize, 0);
        if (from >= list.size()) {
            return PageResponse.<ChatThreadVo>builder()
                    .list(Collections.emptyList())
                    .page(safePage)
                    .size(safeSize)
                    .total(total)
                    .hasMore(Boolean.FALSE)
                    .build();
        }
        int to = (int) Math.min(from + safeSize, list.size());

        return PageResponse.<ChatThreadVo>builder()
                .list(list.subList(from, to))
                .page(safePage)
                .size(safeSize)
                .total(total)
                .hasMore(to < list.size())
                .build();
    }

    /**
     * 打开会话：
     * - 不存在则在互关条件下创建
     * - 存在则补齐 thread_user 行并刷新状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatThreadVo openThread(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw BizException.of(ErrorCode.CHAT_SELF_NOT_ALLOWED);
        }
        mustGetActiveUser(targetUserId);

        long userA = Math.min(currentUserId, targetUserId);
        long userB = Math.max(currentUserId, targetUserId);
        ChatThread thread = chatThreadMapper.selectOne(new LambdaQueryWrapper<ChatThread>()
                .eq(ChatThread::getUserAId, userA)
                .eq(ChatThread::getUserBId, userB)
                .last("LIMIT 1"));

        boolean mutual = isMutualFollow(currentUserId, targetUserId);
        if (thread == null) {
            if (!mutual) {
                throw BizException.of(ErrorCode.CHAT_REQUIRE_MUTUAL_FOLLOW);
            }
            // 首次创建会话时，初始化双方会话状态行
            thread = ChatThread.builder()
                    .userAId(userA)
                    .userBId(userB)
                    .status("ACTIVE")
                    .build();
            chatThreadMapper.insert(thread);
            ensureThreadUser(thread.getId(), currentUserId);
            ensureThreadUser(thread.getId(), targetUserId);
        } else {
            ensureThreadUser(thread.getId(), currentUserId);
            ensureThreadUser(thread.getId(), targetUserId);
            thread = refreshThreadStatusByMutual(thread, currentUserId);
        }

        return toThreadVo(currentUserId, thread, getThreadUser(thread.getId(), currentUserId), loadUserSummaryMap(List.of(targetUserId)).get(targetUserId));
    }

    @Override
    public ChatThreadVo getThread(Long currentUserId, Long threadId) {
        ChatThread thread = mustGetThreadForUser(threadId, currentUserId);
        ChatThread refreshed = refreshThreadStatusByMutual(thread, currentUserId);
        Long peerId = getPeerId(refreshed, currentUserId);
        return toThreadVo(currentUserId, refreshed, getThreadUser(threadId, currentUserId), loadUserSummaryMap(List.of(peerId)).get(peerId));
    }

    /**
     * 拉取历史消息：cursor 为空时返回最近一页。
     */
    @Override
    public PageResponse<ChatMessageVo> listMessages(Long currentUserId, Long threadId, Long cursor, Long size) {
        ChatThread thread = mustGetThreadForUser(threadId, currentUserId);
        refreshThreadStatusByMutual(thread, currentUserId);

        long safeSize = size == null || size < 1 ? 20 : Math.min(size, 100);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getThreadId, threadId)
                .orderByDesc(ChatMessage::getId);
        if (cursor != null && cursor > 0) {
            wrapper.lt(ChatMessage::getId, cursor);
        }
        wrapper.last("LIMIT " + safeSize);

        List<ChatMessage> records = chatMessageMapper.selectList(wrapper);
        if (records.isEmpty()) {
            return PageResponse.<ChatMessageVo>builder()
                    .list(Collections.emptyList())
                    .page(1L)
                    .size(safeSize)
                    .total(0L)
                    .hasMore(Boolean.FALSE)
                    .build();
        }

        Collections.reverse(records);
        List<ChatMessageVo> list = records.stream().map(this::toMessageVo).toList();

        Long minId = records.stream().map(ChatMessage::getId).min(Long::compareTo).orElse(0L);
        boolean hasMore = chatMessageMapper.selectCount(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getThreadId, threadId)
                .lt(ChatMessage::getId, minId)) > 0;

        return PageResponse.<ChatMessageVo>builder()
                .list(list)
                .page(1L)
                .size(safeSize)
                .total(null)
                .hasMore(hasMore)
                .build();
    }

    /**
     * 发送消息：
     * - 强制互关校验
     * - clientMsgId 幂等防重
     * - 更新会话预览与对端未读数
     * - 触发聊天通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageVo sendMessage(Long currentUserId, Long threadId, SendMessageRequest request) {
        ChatThread thread = mustGetThreadForUser(threadId, currentUserId);
        Long receiverId = getPeerId(thread, currentUserId);

        boolean mutual = isMutualFollow(currentUserId, receiverId);
        if (!mutual) {
            // 非互关会话强制切换只读，历史可见但禁止发送
            if (!"READ_ONLY".equals(thread.getStatus())) {
                chatThreadMapper.update(null, new LambdaUpdateWrapper<ChatThread>()
                        .eq(ChatThread::getId, threadId)
                        .set(ChatThread::getStatus, "READ_ONLY"));
            }
            throw BizException.of(ErrorCode.CHAT_SEND_FORBIDDEN_BY_RELATION);
        }
        if (!"ACTIVE".equals(thread.getStatus())) {
            chatThreadMapper.update(null, new LambdaUpdateWrapper<ChatThread>()
                    .eq(ChatThread::getId, threadId)
                    .set(ChatThread::getStatus, "ACTIVE"));
        }

        ChatMessage existed = chatMessageMapper.selectOne(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSenderId, currentUserId)
                .eq(ChatMessage::getClientMsgId, request.getClientMsgId())
                .last("LIMIT 1"));
        if (existed != null) {
            return toMessageVo(existed);
        }

        ChatMessage message = ChatMessage.builder()
                .threadId(threadId)
                .senderId(currentUserId)
                .receiverId(receiverId)
                .clientMsgId(request.getClientMsgId())
                .messageType(request.getMessageType())
                .content(request.getContent())
                .isRead(0)
                .status("SENT")
                .build();
        chatMessageMapper.insert(message);

        String preview = request.getContent().length() <= 60 ? request.getContent() : request.getContent().substring(0, 60);
        chatThreadMapper.update(null, new LambdaUpdateWrapper<ChatThread>()
                .eq(ChatThread::getId, threadId)
                .set(ChatThread::getLastMessageId, message.getId())
                .set(ChatThread::getLastMessagePreview, preview)
                .set(ChatThread::getLastMessageAt, LocalDateTime.now())
                .set(ChatThread::getStatus, "ACTIVE"));

        // 兜底保证双方都有 thread_user 记录
        ensureThreadUser(threadId, currentUserId);
        ensureThreadUser(threadId, receiverId);
        chatThreadUserMapper.update(null, new LambdaUpdateWrapper<ChatThreadUser>()
                .eq(ChatThreadUser::getThreadId, threadId)
                .eq(ChatThreadUser::getUserId, receiverId)
                .setSql("unread_count = unread_count + 1"));

        // 发送消息后给接收方增加聊天通知
        notificationService.create(
                receiverId,
                currentUserId,
                NotificationType.CHAT,
                NotificationTargetType.THREAD,
                threadId,
                "你有新的私信",
                "收到一条新消息"
        );

        chatPushService.pushToUser(receiverId, "chat.message", toMessageVo(message));

        return toMessageVo(message);
    }

    /**
     * 标记会话已读：同步 thread_user 与消息记录的已读状态。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long currentUserId, Long threadId, MarkReadRequest request) {
        ChatThread thread = mustGetThreadForUser(threadId, currentUserId);

        // 标记会话维度已读状态，未读数直接清零
        chatThreadUserMapper.update(null, new LambdaUpdateWrapper<ChatThreadUser>()
                .eq(ChatThreadUser::getThreadId, threadId)
                .eq(ChatThreadUser::getUserId, currentUserId)
                .set(ChatThreadUser::getLastReadMessageId, request.getLastReadMessageId())
                .set(ChatThreadUser::getLastReadAt, LocalDateTime.now())
                .set(ChatThreadUser::getUnreadCount, 0));

        // 批量标记消息已读
        chatMessageMapper.update(null, new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getThreadId, threadId)
                .eq(ChatMessage::getReceiverId, currentUserId)
                .eq(ChatMessage::getIsRead, 0)
                .le(ChatMessage::getId, request.getLastReadMessageId())
                .set(ChatMessage::getIsRead, 1)
                .set(ChatMessage::getReadAt, LocalDateTime.now())
                .set(ChatMessage::getStatus, "READ"));

        chatPushService.pushToUser(getPeerId(thread, currentUserId), "chat.read", Map.of(
                "threadId", threadId,
                "lastReadMessageId", request.getLastReadMessageId(),
                "readerId", currentUserId,
                "readAt", LocalDateTime.now()
        ));
    }

    /**
     * 校验会话存在且属于当前用户。
     */
    private ChatThread mustGetThreadForUser(Long threadId, Long currentUserId) {
        ChatThread thread = chatThreadMapper.selectById(threadId);
        if (thread == null) {
            throw BizException.of(ErrorCode.CHAT_THREAD_NOT_FOUND);
        }
        if (!currentUserId.equals(thread.getUserAId()) && !currentUserId.equals(thread.getUserBId())) {
            throw BizException.of(ErrorCode.CHAT_THREAD_FORBIDDEN);
        }
        return thread;
    }

    /**
     * 按互关关系刷新会话状态：
     * - 互关 -> ACTIVE
     * - 非互关 -> READ_ONLY（仅可读不可发）
     */
    private ChatThread refreshThreadStatusByMutual(ChatThread thread, Long currentUserId) {
        Long peerId = getPeerId(thread, currentUserId);
        boolean mutual = isMutualFollow(currentUserId, peerId);
        String expectStatus = mutual ? "ACTIVE" : "READ_ONLY";
        if (!expectStatus.equals(thread.getStatus())) {
            chatThreadMapper.update(null, new LambdaUpdateWrapper<ChatThread>()
                    .eq(ChatThread::getId, thread.getId())
                    .set(ChatThread::getStatus, expectStatus));
            thread.setStatus(expectStatus);
        }
        return thread;
    }

    /**
     * 判断双方是否互关。
     */
    private boolean isMutualFollow(Long userId, Long peerId) {
        long aToB = userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFolloweeId, peerId)
                .eq(UserFollow::getStatus, "ACTIVE"));
        if (aToB == 0) {
            return false;
        }
        long bToA = userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, peerId)
                .eq(UserFollow::getFolloweeId, userId)
                .eq(UserFollow::getStatus, "ACTIVE"));
        return bToA > 0;
    }

    /**
     * 校验目标用户存在且状态可用。
     */
    private void mustGetActiveUser(Long userId) {
        AppUser user = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getId, userId)
                .eq(AppUser::getIsDeleted, 0)
                .eq(AppUser::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        if (user == null) {
            throw BizException.of(ErrorCode.USER_TARGET_NOT_FOUND);
        }
    }

    /**
     * 获取会话中的对端用户 ID。
     */
    private Long getPeerId(ChatThread thread, Long currentUserId) {
        return currentUserId.equals(thread.getUserAId()) ? thread.getUserBId() : thread.getUserAId();
    }

    private List<Long> loadMutualOrderedIds(Long currentUserId) {
        List<UserFollow> followingAll = userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, currentUserId)
                .eq(UserFollow::getStatus, "ACTIVE")
                .orderByDesc(UserFollow::getUpdatedAt));
        if (followingAll.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> followingIds = followingAll.stream().map(UserFollow::getFolloweeId).toList();
        Set<Long> mutualSet = userFollowMapper.selectList(new LambdaQueryWrapper<UserFollow>()
                        .eq(UserFollow::getFolloweeId, currentUserId)
                        .eq(UserFollow::getStatus, "ACTIVE")
                        .in(UserFollow::getFollowerId, followingIds))
                .stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toSet());
        return followingAll.stream()
                .map(UserFollow::getFolloweeId)
                .filter(mutualSet::contains)
                .toList();
    }

    /**
     * 确保 thread_user 行存在，保障未读统计可更新。
     */
    private void ensureThreadUser(Long threadId, Long userId) {
        ChatThreadUser row = getThreadUser(threadId, userId);
        if (row != null) {
            return;
        }
        chatThreadUserMapper.insert(ChatThreadUser.builder()
                .threadId(threadId)
                .userId(userId)
                .unreadCount(0)
                .build());
    }

    /**
     * 查询单个 thread_user 记录。
     */
    private ChatThreadUser getThreadUser(Long threadId, Long userId) {
        return chatThreadUserMapper.selectOne(new LambdaQueryWrapper<ChatThreadUser>()
                .eq(ChatThreadUser::getThreadId, threadId)
                .eq(ChatThreadUser::getUserId, userId)
                .last("LIMIT 1"));
    }

    /**
     * 批量加载 thread_user 映射。
     */
    private Map<Long, ChatThreadUser> loadThreadUserMap(Long currentUserId, List<Long> threadIds) {
        if (threadIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return chatThreadUserMapper.selectList(new LambdaQueryWrapper<ChatThreadUser>()
                        .eq(ChatThreadUser::getUserId, currentUserId)
                        .in(ChatThreadUser::getThreadId, threadIds))
                .stream()
                .collect(Collectors.toMap(ChatThreadUser::getThreadId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * 批量加载用户摘要信息。
     */
    private Map<Long, UserSummaryVo> loadUserSummaryMap(List<Long> userIds) {
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

    private ChatThreadVo toThreadVo(Long currentUserId, ChatThread thread, ChatThreadUser threadUser, UserSummaryVo peerUser) {
        ChatThreadVo vo = new ChatThreadVo();
        vo.setThreadId(thread.getId());
        vo.setStatus(thread.getStatus());
        vo.setLastMessagePreview(thread.getLastMessagePreview());
        vo.setLastMessageAt(thread.getLastMessageAt());
        vo.setUnreadCount(threadUser == null || threadUser.getUnreadCount() == null ? 0 : threadUser.getUnreadCount());
        vo.setPeerUser(peerUser);
        return vo;
    }

    /**
     * 消息实体转 VO。
     */
    private ChatMessageVo toMessageVo(ChatMessage message) {
        ChatMessageVo vo = new ChatMessageVo();
        vo.setId(message.getId());
        vo.setThreadId(message.getThreadId());
        vo.setSenderId(message.getSenderId());
        vo.setReceiverId(message.getReceiverId());
        vo.setClientMsgId(message.getClientMsgId());
        vo.setMessageType(message.getMessageType());
        vo.setContent(message.getContent());
        vo.setRead(message.getIsRead() != null && message.getIsRead() == 1);
        vo.setReadAt(message.getReadAt());
        vo.setCreatedAt(message.getCreatedAt());
        return vo;
    }
}
