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

    @NotBlank(message = "客户端消息ID不能为空")
    @Size(max = 64, message = "客户端消息ID长度不能超过64位")
    private String clientMsgId;

    @NotBlank(message = "消息类型不能为空")
    @Pattern(regexp = "TEXT|IMAGE", message = "消息类型仅支持 TEXT 或 IMAGE")
    private String messageType = "TEXT";

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 5000, message = "消息内容长度不能超过5000位")
    private String content;
}
