package com.community.backend.dto.post;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * 帖子列表查询参数：分页、筛选与排序。
 */
public class PostQueryRequest {

    @Min(value = 1, message = "页码必须大于等于1")
    private Long page = 1L;

    @Min(value = 1, message = "分页大小必须大于等于1")
    private Long size = 20L;

    @Pattern(regexp = "latest|hot", message = "排序字段仅支持 latest 或 hot")
    private String sort = "latest";

    private Long tagId;
    private Long authorId;

    @Size(max = 64, message = "关键词长度不能超过64位")
    private String keyword;
}
