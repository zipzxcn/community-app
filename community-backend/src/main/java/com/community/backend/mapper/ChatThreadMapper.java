package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.ChatThread;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 聊天会话 Mapper。
 */
public interface ChatThreadMapper extends BaseMapper<ChatThread> {
}
