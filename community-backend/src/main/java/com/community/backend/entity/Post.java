package com.community.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("post")
/**
 * 帖子实体：正文内容与互动统计。
 */
public class Post {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 帖子作者用户 ID。
     */
    private Long authorId;
    /**
     * 标题文本，用于帖子、通知等场景。
     */
    private String title;
    /**
     * 正文摘要，主要用于帖子列表卡片。
     */
    private String excerpt;
    /**
     * Markdown 原始内容，便于再次编辑。
     */
    private String contentMd;
    /**
     * 服务端渲染后的 HTML 内容，便于详情页直接展示。
     */
    private String contentHtml;
    /**
     * 封面图地址。
     */
    private String coverUrl;
    /**
     * 业务状态字段，不同对象有各自的枚举语义，例如 ACTIVE、PUBLISHED、HIDDEN。
     */
    private String status;
    /**
     * 是否允许评论，1/0 或 true/false 取决于所在层的表示方式。
     */
    private Integer allowComment;
    /**
     * 浏览次数统计。
     */
    private Integer viewCount;
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
     * 逻辑删除标记，0 表示未删除，1 表示已删除。
     */
    private Integer isDeleted;
    /**
     * 发布时间，未发布数据通常为空。
     */
    private LocalDateTime publishedAt;
    /**
     * 删除时间，用于软删除审计。
     */
    private LocalDateTime deletedAt;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
}
