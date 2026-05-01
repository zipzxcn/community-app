package com.community.backend.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * 发表评论请求参数。
 */
public class CreateCommentRequest {

    /**
     * 正文内容，帖子、评论、消息等核心文本都通过该字段承载。
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000位")
    private String content;
}
