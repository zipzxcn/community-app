package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 聊天消息 Mapper。
 */
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
