package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息 Mapper：
 * - 负责消息明细表的增删改查。
 * - 会话历史、已读推进、最后消息摘要都建立在该表事实上。
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
