package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.ChatThreadUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 会话用户关系 Mapper：维护每个用户在会话中的已读和未读状态。
 */
public interface ChatThreadUserMapper extends BaseMapper<ChatThreadUser> {
}
