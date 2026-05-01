package com.community.backend.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminPostQueryRequest {

    /**
     * 当前页码，前端分页从 1 开始。
     */
    @Min(value = 1, message = "页码必须大于等于1")
    private Long page = 1L;

    /**
     * 每页条数，用于限制单次查询返回量。
     */
    @Min(value = 1, message = "分页大小必须大于等于1")
    private Long size = 10L;

    /**
     * 后台帖子筛选状态，ALL 表示全部，PUBLISHED/HIDDEN 表示按可见性过滤。
     */
    @Pattern(regexp = "ALL|PUBLISHED|HIDDEN", message = "状态仅支持 ALL、PUBLISHED 或 HIDDEN")
    private String status = "ALL";

    /**
     * 模糊搜索关键词。
     */
    @Size(max = 64, message = "关键词长度不能超过64位")
    private String keyword;
}
