package com.community.backend.vo.draft;

import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 草稿列表项返回体。
 */
public class DraftItemVo {

    private Long id;
    private String title;
    private String coverUrl;
    private LocalDateTime autoSavedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
