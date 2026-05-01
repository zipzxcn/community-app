package com.community.backend.dto.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * 会话已读请求参数：指定最后已读消息 ID。
 */
public class MarkReadRequest {

    /**
     * 最后一条已读消息 ID，用于按游标推进已读状态。
     */
    @NotNull(message = "最后已读消息ID不能为空")
    private Long lastReadMessageId;
}
