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

    private Long id;
    private Long postId;
    private Long parentId;
    private Long rootId;
    private String content;
    private Integer likeCount;
    private Integer replyCount;
    private String status;
    private Boolean liked;
    private LocalDateTime createdAt;
    private UserSummaryVo user;
    private UserSummaryVo replyToUser;
    private List<CommentItemVo> replies;
}
