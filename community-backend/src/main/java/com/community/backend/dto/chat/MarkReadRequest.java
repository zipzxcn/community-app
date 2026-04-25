package com.community.backend.dto.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * 会话已读请求参数：指定最后已读消息 ID。
 */
public class MarkReadRequest {

    @NotNull(message = "最后已读消息ID不能为空")
    private Long lastReadMessageId;
}
