package com.community.backend.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * 发送消息请求参数：客户端消息唯一 ID、类型和正文。
 */
public class SendMessageRequest {

    /**
     * 客户端生成的消息唯一标识，用于前后端幂等和去重。
     */
    @NotBlank(message = "客户端消息ID不能为空")
    @Size(max = 64, message = "客户端消息ID长度不能超过64位")
    private String clientMsgId;

    /**
     * 消息类型，当前主要支持 TEXT 和 IMAGE。
     */
    @NotBlank(message = "消息类型不能为空")
    @Pattern(regexp = "TEXT|IMAGE", message = "消息类型仅支持 TEXT 或 IMAGE")
    private String messageType = "TEXT";

    /**
     * 消息正文；TEXT 时为文本，IMAGE 时通常为图片 URL。
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 5000, message = "消息内容长度不能超过5000位")
    private String content;
}
