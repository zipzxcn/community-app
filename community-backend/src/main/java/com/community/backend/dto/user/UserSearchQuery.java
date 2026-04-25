package com.community.backend.dto.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * 用户搜索查询参数：关键词与分页信息。
 */
public class UserSearchQuery {

    @Size(max = 32, message = "关键词长度不能超过32位")
    private String keyword;

    @Min(value = 1, message = "页码必须大于等于1")
    private Long page = 1L;

    @Min(value = 1, message = "分页大小必须大于等于1")
    private Long size = 20L;
}
