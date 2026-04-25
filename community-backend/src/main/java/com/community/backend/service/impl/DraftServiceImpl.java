package com.community.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.backend.common.api.PageResponse;
import com.community.backend.common.error.BizException;
import com.community.backend.common.error.ErrorCode;
import com.community.backend.dto.draft.SaveDraftRequest;
import com.community.backend.dto.post.CreatePostRequest;
import com.community.backend.entity.PostDraft;
import com.community.backend.mapper.PostDraftMapper;
import com.community.backend.service.DraftService;
import com.community.backend.service.PostService;
import com.community.backend.vo.draft.DraftDetailVo;
import com.community.backend.vo.draft.DraftItemVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 草稿服务实现：草稿 CRUD 与一键发布。
 */
@Service
public class DraftServiceImpl implements DraftService {

    private final PostDraftMapper postDraftMapper;
    private final PostService postService;

    public DraftServiceImpl(PostDraftMapper postDraftMapper, PostService postService) {
        this.postDraftMapper = postDraftMapper;
        this.postService = postService;
    }

    /**
     * 草稿列表：仅返回当前用户 ACTIVE 草稿，按更新时间倒序。
     */
    @Override
    public PageResponse<DraftItemVo> list(Long currentUserId, Long page, Long size) {
        Page<PostDraft> pager = new Page<>(page, size);
        Page<PostDraft> result = postDraftMapper.selectPage(pager, new LambdaQueryWrapper<PostDraft>()
                .eq(PostDraft::getUserId, currentUserId)
                .eq(PostDraft::getStatus, "ACTIVE")
                .orderByDesc(PostDraft::getUpdatedAt));
        if (result.getRecords().isEmpty()) {
            return PageResponse.<DraftItemVo>builder()
                    .list(Collections.emptyList())
                    .page(result.getCurrent())
                    .size(result.getSize())
                    .total(result.getTotal())
                    .hasMore(Boolean.FALSE)
                    .build();
        }

        List<DraftItemVo> list = result.getRecords().stream().map(this::toItemVo).toList();
        return PageResponse.<DraftItemVo>builder()
                .list(list)
                .page(result.getCurrent())
                .size(result.getSize())
                .total(result.getTotal())
                .hasMore(result.getCurrent() * result.getSize() < result.getTotal())
                .build();
    }

    /**
     * 草稿详情：仅允许查看自己的 ACTIVE 草稿。
     */
    @Override
    public DraftDetailVo detail(Long currentUserId, Long draftId) {
        return toDetailVo(mustGetActiveDraft(currentUserId, draftId));
    }

    /**
     * 新建草稿：初始化为 ACTIVE 状态。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(Long currentUserId, SaveDraftRequest request) {
        LocalDateTime now = LocalDateTime.now();
        PostDraft draft = PostDraft.builder()
                .userId(currentUserId)
                .title(request.getTitle())
                .contentMd(request.getContentMd())
                .coverUrl(request.getCoverUrl())
                .status("ACTIVE")
                .autoSavedAt(Boolean.TRUE.equals(request.getAutoSave()) ? now : null)
                .build();
        postDraftMapper.insert(draft);
        return draft.getId();
    }

    /**
     * 更新草稿：支持手动保存与自动保存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DraftDetailVo update(Long currentUserId, Long draftId, SaveDraftRequest request) {
        PostDraft draft = mustGetActiveDraft(currentUserId, draftId);
        LambdaUpdateWrapper<PostDraft> updateWrapper = new LambdaUpdateWrapper<PostDraft>()
                .eq(PostDraft::getId, draft.getId())
                .set(PostDraft::getTitle, request.getTitle())
                .set(PostDraft::getContentMd, request.getContentMd())
                .set(PostDraft::getCoverUrl, request.getCoverUrl());
        if (Boolean.TRUE.equals(request.getAutoSave())) {
            updateWrapper.set(PostDraft::getAutoSavedAt, LocalDateTime.now());
        }
        postDraftMapper.update(null, updateWrapper);
        return toDetailVo(postDraftMapper.selectById(draft.getId()));
    }

    /**
     * 删除草稿：逻辑删除为 DISCARDED。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long currentUserId, Long draftId) {
        PostDraft draft = mustGetDraft(currentUserId, draftId);
        if ("PUBLISHED".equals(draft.getStatus())) {
            throw BizException.of(ErrorCode.DRAFT_ALREADY_PUBLISHED);
        }
        postDraftMapper.update(null, new LambdaUpdateWrapper<PostDraft>()
                .eq(PostDraft::getId, draft.getId())
                .set(PostDraft::getStatus, "DISCARDED"));
    }

    /**
     * 草稿发布：
     * 1) 校验草稿仍为 ACTIVE 且正文不为空
     * 2) 发布帖子
     * 3) 回写草稿状态为 PUBLISHED
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(Long currentUserId, Long draftId) {
        PostDraft draft = mustGetDraft(currentUserId, draftId);
        if ("PUBLISHED".equals(draft.getStatus())) {
            throw BizException.of(ErrorCode.DRAFT_ALREADY_PUBLISHED);
        }
        if (!"ACTIVE".equals(draft.getStatus())) {
            throw BizException.of(ErrorCode.DRAFT_NOT_FOUND);
        }
        if (!StringUtils.hasText(draft.getContentMd())) {
            throw BizException.of(ErrorCode.DRAFT_CONTENT_EMPTY);
        }

        CreatePostRequest request = new CreatePostRequest();
        request.setTitle(draft.getTitle());
        request.setContentMd(draft.getContentMd());
        request.setCoverUrl(draft.getCoverUrl());
        request.setAllowComment(Boolean.TRUE);
        request.setTagIds(Collections.emptyList());
        request.setAttachmentFileIds(Collections.emptyList());

        Long postId = postService.create(currentUserId, request);
        postDraftMapper.update(null, new LambdaUpdateWrapper<PostDraft>()
                .eq(PostDraft::getId, draft.getId())
                .set(PostDraft::getStatus, "PUBLISHED")
                .set(PostDraft::getPublishedPostId, postId));
        return postId;
    }

    /**
     * 获取当前用户草稿（不限状态）。
     */
    private PostDraft mustGetDraft(Long currentUserId, Long draftId) {
        PostDraft draft = postDraftMapper.selectOne(new LambdaQueryWrapper<PostDraft>()
                .eq(PostDraft::getId, draftId)
                .eq(PostDraft::getUserId, currentUserId)
                .last("LIMIT 1"));
        if (draft == null) {
            throw BizException.of(ErrorCode.DRAFT_NOT_FOUND);
        }
        return draft;
    }

    /**
     * 获取当前用户 ACTIVE 草稿。
     */
    private PostDraft mustGetActiveDraft(Long currentUserId, Long draftId) {
        PostDraft draft = postDraftMapper.selectOne(new LambdaQueryWrapper<PostDraft>()
                .eq(PostDraft::getId, draftId)
                .eq(PostDraft::getUserId, currentUserId)
                .eq(PostDraft::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        if (draft == null) {
            throw BizException.of(ErrorCode.DRAFT_NOT_FOUND);
        }
        return draft;
    }

    /**
     * 草稿实体转列表项 VO。
     */
    private DraftItemVo toItemVo(PostDraft draft) {
        DraftItemVo vo = new DraftItemVo();
        vo.setId(draft.getId());
        vo.setTitle(draft.getTitle());
        vo.setCoverUrl(draft.getCoverUrl());
        vo.setAutoSavedAt(draft.getAutoSavedAt());
        vo.setUpdatedAt(draft.getUpdatedAt());
        vo.setCreatedAt(draft.getCreatedAt());
        return vo;
    }

    /**
     * 草稿实体转详情 VO。
     */
    private DraftDetailVo toDetailVo(PostDraft draft) {
        DraftDetailVo vo = new DraftDetailVo();
        vo.setId(draft.getId());
        vo.setTitle(draft.getTitle());
        vo.setContentMd(draft.getContentMd());
        vo.setCoverUrl(draft.getCoverUrl());
        vo.setStatus(draft.getStatus());
        vo.setPublishedPostId(draft.getPublishedPostId());
        vo.setAutoSavedAt(draft.getAutoSavedAt());
        vo.setCreatedAt(draft.getCreatedAt());
        vo.setUpdatedAt(draft.getUpdatedAt());
        return vo;
    }
}
