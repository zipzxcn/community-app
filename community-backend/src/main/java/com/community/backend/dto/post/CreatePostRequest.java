package com.community.backend.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
/**
 * 发帖请求参数：标题、内容、标签、附件等。
 */
public class CreatePostRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题长度不能超过120位")
    private String title;

    @NotBlank(message = "正文不能为空")
    private String contentMd;

    @Size(max = 255, message = "封面地址长度不能超过255位")
    private String coverUrl;

    private Boolean allowComment = Boolean.TRUE;

    private List<Long> tagIds;

    private List<Long> attachmentFileIds;
}
