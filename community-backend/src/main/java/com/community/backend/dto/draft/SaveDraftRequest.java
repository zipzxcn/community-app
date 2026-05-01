package com.community.backend.dto.draft;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
/**
 * 保存草稿请求参数。
 */
public class SaveDraftRequest {

    /**
     * 标题文本，用于帖子、通知等场景。
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题长度不能超过120位")
    private String title;

    /**
     * Markdown 原始内容，便于再次编辑。
     */
    private String contentMd;
    /**
     * 封面图地址。
     */
    private String coverUrl;
    /**
     * 标签 ID 列表，用于发帖/草稿保存时关联标签。
     */
    private List<Long> tagIds;
    /**
     * 附件文件 ID 列表，便于前后端只传引用。
     */
    private List<Long> attachmentFileIds;
    private Boolean autoSave = Boolean.FALSE;
}
