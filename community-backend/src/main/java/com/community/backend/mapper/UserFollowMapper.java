package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 用户关注关系 Mapper。
 */
public interface UserFollowMapper extends BaseMapper<UserFollow> {
}
