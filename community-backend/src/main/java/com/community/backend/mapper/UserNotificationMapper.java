package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.UserNotification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户通知 Mapper：
 * - 未读统计、列表筛选、批量已读都围绕该表操作。
 * - 通过统一通知表承接点赞、评论、关注、聊天等多种事件。
 */
@Mapper
public interface UserNotificationMapper extends BaseMapper<UserNotification> {
}
