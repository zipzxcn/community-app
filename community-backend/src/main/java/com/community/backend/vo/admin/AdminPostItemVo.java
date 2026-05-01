package com.community.backend.vo.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminPostItemVo {

    /**
     * 帖子 ID。
     */
    private Long id;
    /**
     * 标题文本，用于帖子、通知等场景。
     */
    private String title;
    /**
     * 正文摘要，主要用于帖子列表卡片。
     */
    private String excerpt;
    /**
     * 业务状态字段，不同对象有各自的枚举语义，例如 ACTIVE、PUBLISHED、HIDDEN。
     */
    private String status;
    /**
     * 帖子作者用户 ID。
     */
    private Long authorId;
    /**
     * 作者展示名，便于后台管理列表直接展示。
     */
    private String authorName;
    /**
     * 点赞数量统计。
     */
    private Integer likeCount;
    /**
     * 收藏数量统计。
     */
    private Integer favoriteCount;
    /**
     * 评论类未读数量或评论总数。
     */
    private Integer commentCount;
    /**
     * 浏览次数统计。
     */
    private Integer viewCount;
    /**
     * 发布时间，未发布数据通常为空。
     */
    private LocalDateTime publishedAt;
    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
}
