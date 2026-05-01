package com.community.backend.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * 回复评论请求参数：父评论 ID 与回复内容。
 */
public class ReplyCommentRequest {

    /**
     * 父评论 ID，根评论为空。
     */
    @NotNull(message = "父评论ID不能为空")
    private Long parentId;

    /**
     * 正文内容，帖子、评论、消息等核心文本都通过该字段承载。
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容长度不能超过1000位")
    private String content;
}
