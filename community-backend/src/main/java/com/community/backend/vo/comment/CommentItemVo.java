package com.community.backend.vo.comment;

import com.community.backend.vo.user.UserSummaryVo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
/**
 * 评论项返回体：支持树形 replies。
 */
public class CommentItemVo {

    /**
     * 评论 ID。
     */
    private Long id;
    /**
     * 帖子 ID，关联帖子主表。
     */
    private Long postId;
    /**
     * 父评论 ID，根评论为空。
     */
    private Long parentId;
    /**
     * 评论树根节点 ID，用于快速聚合同一主题下的回复。
     */
    private Long rootId;
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
     * 当前登录用户是否已点赞。
     */
    private Boolean liked;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 评论作者或记录所属用户的概要信息。
     */
    private UserSummaryVo user;
    /**
     * 被回复用户的概要信息。
     */
    private UserSummaryVo replyToUser;
    /**
     * 当前评论下挂载的回复列表。
     */
    private List<CommentItemVo> replies;
}
