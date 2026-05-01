package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户关注关系 Mapper：
 * - 互关判断、粉丝列表、关注列表等都依赖该表。
 * - 也是聊天“仅互关可私信”规则的底层数据来源。
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
}
