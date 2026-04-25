package com.community.backend.vo.draft;

import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 草稿详情返回体。
 */
public class DraftDetailVo {

    private Long id;
    private String title;
    private String contentMd;
    private String coverUrl;
    private String status;
    private Long publishedPostId;
    private LocalDateTime autoSavedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
