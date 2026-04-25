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

    @NotNull(message = "父评论ID不能为空")
    private Long parentId;

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 1000, message = "回复内容长度不能超过1000位")
    private String content;
}
