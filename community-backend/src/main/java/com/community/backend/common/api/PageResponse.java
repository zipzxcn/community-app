package com.community.backend.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * 当前页数据列表。
     */
    @Builder.Default
    private List<T> list = Collections.emptyList();
    /**
     * 当前页码，前端分页从 1 开始。
     */
    private Long page;
    /**
     * 每页条数，用于限制单次查询返回量。
     */
    private Long size;
    /**
     * 总未读数或总记录数，具体取决于对象语义。
     */
    private Long total;
    /**
     * 是否还有下一页，便于前端决定是否继续翻页。
     */
    private Boolean hasMore;
}
