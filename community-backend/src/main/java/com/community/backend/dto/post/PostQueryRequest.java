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

    /**
     * 排序规则，控制列表按最新或热门等方式返回。
     */
    @Pattern(regexp = "latest|hot", message = "排序字段仅支持 latest 或 hot")
    private String sort = "latest";

    /**
     * 标签 ID。
     */
    private Long tagId;
    /**
     * 帖子作者用户 ID。
     */
    private Long authorId;

    /**
     * 模糊搜索关键词。
     */
    @Size(max = 64, message = "关键词长度不能超过64位")
    private String keyword;
}
