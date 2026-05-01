package com.community.backend.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminPostItemVo {

    private Long id;
    private String title;
    private String excerpt;
    private String status;
    private Long authorId;
    private String authorName;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer viewCount;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;
}
