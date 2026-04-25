package com.community.backend.service;

import com.community.backend.common.api.PageResponse;
import com.community.backend.dto.comment.CreateCommentRequest;
import com.community.backend.dto.comment.ReplyCommentRequest;
import com.community.backend.vo.comment.CommentItemVo;

public interface CommentService {

    CommentItemVo createComment(Long currentUserId, Long postId, CreateCommentRequest request);

    CommentItemVo reply(Long currentUserId, Long commentId, ReplyCommentRequest request);

    void deleteComment(Long currentUserId, Long commentId);

    void like(Long currentUserId, Long commentId);

    void unlike(Long currentUserId, Long commentId);

    PageResponse<CommentItemVo> listByPost(Long postId, Long currentUserId, Long page, Long size);
}
