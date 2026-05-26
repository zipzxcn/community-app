package com.community.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.backend.common.api.PageResponse;
import com.community.backend.common.constant.NotificationType;
import com.community.backend.entity.AppUser;
import com.community.backend.entity.UserNotification;
import com.community.backend.mapper.AppUserMapper;
import com.community.backend.mapper.UserNotificationMapper;
import com.community.backend.service.NotificationService;
import com.community.backend.vo.notification.NotificationItemVo;
import com.community.backend.vo.notification.NotificationUnreadVo;
import com.community.backend.vo.user.UserSummaryVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通知服务实现：通知创建、分页查询与已读状态维护。
 * 通知服务实现：
 * - 统一承接点赞、评论、关注、聊天等系统内事件。
 * - 让其它业务模块只关心“创建通知”，无需各自处理未读统计。
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final UserNotificationMapper userNotificationMapper;
    private final AppUserMapper appUserMapper;

    public NotificationServiceImpl(UserNotificationMapper userNotificationMapper, AppUserMapper appUserMapper) {
        this.userNotificationMapper = userNotificationMapper;
        this.appUserMapper = appUserMapper;
    }

    /**
     * 创建通知：
     * - receiverId 为空时忽略
     * - actor==receiver 时忽略（避免自己给自己发通知）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Long receiverId, Long actorId, String type, String targetType, Long targetId, String title, String content) {
        // 避免给自己发送通知
        if (receiverId == null || receiverId <= 0) {
            return;
        }
        if (actorId != null && receiverId.equals(actorId)) {
            return;
        }

        // 通知统一落表，前端再按 type/targetType 做分类展示。
        UserNotification notification = UserNotification.builder()
                .receiverId(receiverId)
                .actorId(actorId)
                .type(type)
                .targetType(targetType)
                .targetId(targetId)
                .title(title)
                .content(content)
                .isRead(0)
                .build();
        userNotificationMapper.insert(notification);
    }

    /**
     * 通知列表分页查询，支持多维度过滤。
     */
    @Override
    public PageResponse<NotificationItemVo> list(Long currentUserId,
                                                 String type,
                                                 String targetType,
                                                 Boolean isRead,
                                                 String keyword,
                                                 Long page,
                                                 Long size) {
        Page<UserNotification> pager = new Page<>(page, size);
        // 构建查询条件
        LambdaQueryWrapper<UserNotification> wrapper = new LambdaQueryWrapper<UserNotification>()
                .eq(UserNotification::getReceiverId, currentUserId)
                .orderByDesc(UserNotification::getCreatedAt);
        if (StringUtils.hasText(type)) {
            wrapper.eq(UserNotification::getType, type);
        }
        if (StringUtils.hasText(targetType)) {
            wrapper.eq(UserNotification::getTargetType, targetType);
        }
        if (isRead != null) {
            wrapper.eq(UserNotification::getIsRead, Boolean.TRUE.equals(isRead) ? 1 : 0);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(UserNotification::getTitle, keyword)
                    .or()
                    .like(UserNotification::getContent, keyword));
        }

        Page<UserNotification> result = userNotificationMapper.selectPage(pager, wrapper);
        if (result.getRecords().isEmpty()) {
            return PageResponse.<NotificationItemVo>builder()
                    .list(Collections.emptyList())
                    .page(result.getCurrent())
                    .size(result.getSize())
                    .total(result.getTotal())
                    .hasMore(Boolean.FALSE)
                    .build();
        }

        Map<Long, UserSummaryVo> actorMap = loadActorMap(result.getRecords());
        List<NotificationItemVo> list = result.getRecords().stream().map(notification -> toVo(notification, actorMap)).toList();
        return PageResponse.<NotificationItemVo>builder()
                .list(list)
                .page(result.getCurrent())
                .size(result.getSize())
                .total(result.getTotal())
                .hasMore(result.getCurrent() * result.getSize() < result.getTotal())
                .build();
    }

    /**
     * 统计未读数：总计 + 分类计数。
     */
    @Override
    public NotificationUnreadVo unreadCount(Long currentUserId) {
        NotificationUnreadVo vo = new NotificationUnreadVo();
        vo.setPostLikeCount(countByType(currentUserId, NotificationType.POST_LIKE));
        vo.setCommentCount(countByType(currentUserId, NotificationType.COMMENT));
        vo.setFollowCount(countByType(currentUserId, NotificationType.FOLLOW));
        vo.setChatCount(countByType(currentUserId, NotificationType.CHAT));
        vo.setSystemCount(countByType(currentUserId, NotificationType.SYSTEM));
        vo.setTotal(vo.getPostLikeCount() + vo.getCommentCount() + vo.getFollowCount() + vo.getChatCount() + vo.getSystemCount());
        return vo;
    }

    /**
     * 标记单条通知已读（仅允许本人操作）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long currentUserId, Long notificationId) {
        // 只允许当前用户标记自己的通知
        userNotificationMapper.update(null, new LambdaUpdateWrapper<UserNotification>()
                .eq(UserNotification::getId, notificationId)
                .eq(UserNotification::getReceiverId, currentUserId)
                .eq(UserNotification::getIsRead, 0)
                .set(UserNotification::getIsRead, 1)
                .set(UserNotification::getReadAt, LocalDateTime.now()));
    }

    /**
     * 批量标记已读（可按 type 过滤）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long currentUserId, String type) {
        LambdaUpdateWrapper<UserNotification> wrapper = new LambdaUpdateWrapper<UserNotification>()
                .eq(UserNotification::getReceiverId, currentUserId)
                .eq(UserNotification::getIsRead, 0)
                .set(UserNotification::getIsRead, 1)
                .set(UserNotification::getReadAt, LocalDateTime.now());
        if (StringUtils.hasText(type)) {
            wrapper.eq(UserNotification::getType, type);
        }
        userNotificationMapper.update(null, wrapper);
    }

    /**
     * 统计某个通知类型的未读数。
     */
    private Integer countByType(Long userId, String type) {
        Long count = userNotificationMapper.selectCount(new LambdaQueryWrapper<UserNotification>()
                .eq(UserNotification::getReceiverId, userId)
                .eq(UserNotification::getType, type)
                .eq(UserNotification::getIsRead, 0));
        return count == null ? 0 : count.intValue();
    }

    /**
     * 加载通知触发者信息（actor）。
     */
    private Map<Long, UserSummaryVo> loadActorMap(List<UserNotification> notifications) {
        List<Long> actorIds = notifications.stream()
                .map(UserNotification::getActorId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (actorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return appUserMapper.selectList(new LambdaQueryWrapper<AppUser>()
                        .in(AppUser::getId, new ArrayList<>(actorIds))
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
     * 通知实体转前端 VO。
     */
    private NotificationItemVo toVo(UserNotification notification, Map<Long, UserSummaryVo> actorMap) {
        NotificationItemVo vo = new NotificationItemVo();
        vo.setId(notification.getId());
        vo.setActorId(notification.getActorId());
        vo.setType(notification.getType());
        vo.setTargetType(notification.getTargetType());
        vo.setTargetId(notification.getTargetId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setRead(notification.getIsRead() != null && notification.getIsRead() == 1);
        vo.setReadAt(notification.getReadAt());
        vo.setCreatedAt(notification.getCreatedAt());
        vo.setActor(actorMap.get(notification.getActorId()));
        return vo;
    }
}
