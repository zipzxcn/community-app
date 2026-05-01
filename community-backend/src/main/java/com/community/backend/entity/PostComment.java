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
@TableName("post_comment")
/**
 * 帖子评论实体：支持根评论与回复层级。
 */
public class PostComment {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 帖子 ID，关联帖子主表。
     */
    private Long postId;
    /**
     * 当前业务记录所属的用户 ID。
     */
    private Long userId;
    /**
     * 父评论 ID，根评论为空。
     */
    private Long parentId;
    /**
     * 评论树根节点 ID，用于快速聚合同一主题下的回复。
     */
    private Long rootId;
    /**
     * 被回复的目标用户 ID。
     */
    private Long replyToUserId;
    /**
     * 正文内容，帖子、评论、消息等核心文本都通过该字段承载。
     */
    private String content;
    /**
     * 点赞数量统计。
     */
    private Integer likeCount;
    /**
     * 回复数量统计。
     */
    private Integer replyCount;
    /**
     * 业务状态字段，不同对象有各自的枚举语义，例如 ACTIVE、PUBLISHED、HIDDEN。
     */
    private String status;
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
