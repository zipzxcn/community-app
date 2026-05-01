package com.community.backend.dto.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * 用户搜索查询参数：关键词与分页信息。
 */
public class UserSearchQuery {

    /**
     * 模糊搜索关键词。
     */
    @Size(max = 32, message = "关键词长度不能超过32位")
    private String keyword;

    /**
     * 当前页码，前端分页从 1 开始。
     */
    @Min(value = 1, message = "页码必须大于等于1")
    private Long page = 1L;

    /**
     * 每页条数，用于限制单次查询返回量。
     */
    @Min(value = 1, message = "分页大小必须大于等于1")
    private Long size = 20L;
}
