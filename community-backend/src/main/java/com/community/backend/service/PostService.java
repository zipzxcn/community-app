package com.community.backend.service;

import com.community.backend.common.api.PageResponse;
import com.community.backend.dto.post.CreatePostRequest;
import com.community.backend.dto.post.PostQueryRequest;
import com.community.backend.vo.post.PostDetailVo;
import com.community.backend.vo.post.PostListItemVo;

public interface PostService {

    Long create(Long currentUserId, CreatePostRequest request);

    PageResponse<PostListItemVo> list(PostQueryRequest query, Long currentUserId);

    PostDetailVo detail(Long postId, Long currentUserId);

    void like(Long currentUserId, Long postId);

    void unlike(Long currentUserId, Long postId);

    void favorite(Long currentUserId, Long postId);

    void unfavorite(Long currentUserId, Long postId);
}
