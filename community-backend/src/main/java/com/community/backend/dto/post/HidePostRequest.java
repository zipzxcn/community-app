package com.community.backend.dto.post;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
/**
 * 帖子显隐请求参数：hidden=true 表示下架，false 表示恢复上架。
 */
public class HidePostRequest {

    @NotNull(message = "hidden 不能为空")
    private Boolean hidden;
}
