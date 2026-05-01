package com.community.backend.vo.draft;

import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 草稿列表项返回体。
 */
public class DraftItemVo {

    /**
     * 草稿 ID。
     */
    private Long id;
    /**
     * 标题文本，用于帖子、通知等场景。
     */
    private String title;
    /**
     * 封面图地址。
     */
    private String coverUrl;
    /**
     * 最近一次自动保存时间。
     */
    private LocalDateTime autoSavedAt;
    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
