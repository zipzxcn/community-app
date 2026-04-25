package com.community.backend.dto.draft;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * 保存草稿请求参数。
 */
public class SaveDraftRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题长度不能超过120位")
    private String title;

    private String contentMd;
    private String coverUrl;
    private Boolean autoSave = Boolean.FALSE;
}
