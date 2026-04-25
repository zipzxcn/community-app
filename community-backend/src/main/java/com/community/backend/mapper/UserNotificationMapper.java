package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.UserNotification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 用户通知 Mapper。
 */
public interface UserNotificationMapper extends BaseMapper<UserNotification> {
}
