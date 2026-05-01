package com.community.backend.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
/**
 * 更新帖子请求参数（预留）。
 */
public class UpdatePostRequest {

    /**
     * 标题文本，用于帖子、通知等场景。
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题长度不能超过120位")
    private String title;

    /**
     * Markdown 原始内容，便于再次编辑。
     */
    @NotBlank(message = "正文不能为空")
    private String contentMd;

    /**
     * 封面图地址。
     */
    @Size(max = 255, message = "封面地址长度不能超过255位")
    private String coverUrl;

    /**
     * 是否允许评论，1/0 或 true/false 取决于所在层的表示方式。
     */
    private Boolean allowComment = Boolean.TRUE;
    /**
     * 标签 ID 列表，用于发帖/草稿保存时关联标签。
     */
    private List<Long> tagIds;
    /**
     * 附件文件 ID 列表，便于前后端只传引用。
     */
    private List<Long> attachmentFileIds;
}
