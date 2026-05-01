package com.community.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.backend.common.error.BizException;
import com.community.backend.common.api.PageResponse;
import com.community.backend.common.error.ErrorCode;
import com.community.backend.dto.draft.SaveDraftRequest;
import com.community.backend.dto.post.CreatePostRequest;
import com.community.backend.entity.FileObject;
import com.community.backend.entity.PostDraft;
import com.community.backend.entity.Tag;
import com.community.backend.mapper.FileObjectMapper;
import com.community.backend.mapper.PostDraftMapper;
import com.community.backend.mapper.TagMapper;
import com.community.backend.service.DraftService;
import com.community.backend.service.PostService;
import com.community.backend.vo.draft.DraftDetailVo;
import com.community.backend.vo.draft.DraftItemVo;
import com.community.backend.vo.file.FileObjectVo;
import com.community.backend.vo.post.TagVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 草稿服务实现：草稿 CRUD 与一键发布。
 */
/**
 * 草稿服务实现：
 * - 解决发帖流程中“内容还没准备好但不想丢失输入”的问题。
 * - 支持手动保存、自动保存、草稿发布转正式帖子。
 */
@Service
public class DraftServiceImpl implements DraftService {

    private final PostDraftMapper postDraftMapper;
    private final PostService postService;
    private final TagMapper tagMapper;
    private final FileObjectMapper fileObjectMapper;
    private final ObjectMapper objectMapper;

    public DraftServiceImpl(PostDraftMapper postDraftMapper,
                            PostService postService,
                            TagMapper tagMapper,
                            FileObjectMapper fileObjectMapper,
                            ObjectMapper objectMapper) {
        this.postDraftMapper = postDraftMapper;
        this.postService = postService;
        this.tagMapper = tagMapper;
        this.fileObjectMapper = fileObjectMapper;
        this.objectMapper = objectMapper;
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
        List<Long> tagIds = normalizeIds(request.getTagIds());
        List<Long> attachmentFileIds = normalizeIds(request.getAttachmentFileIds());
        validateTagIds(tagIds);
        validateAttachmentFileIds(currentUserId, attachmentFileIds);
        // 草稿直接保存 Markdown、标签、附件引用，方便后续继续编辑或一键发布。
        PostDraft draft = PostDraft.builder()
                .userId(currentUserId)
                .title(request.getTitle())
                .contentMd(request.getContentMd())
                .coverUrl(request.getCoverUrl())
                .tagIdsJson(writeIdList(tagIds))
                .attachmentFileIdsJson(writeIdList(attachmentFileIds))
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
        List<Long> tagIds = normalizeIds(request.getTagIds());
        List<Long> attachmentFileIds = normalizeIds(request.getAttachmentFileIds());
        validateTagIds(tagIds);
        validateAttachmentFileIds(currentUserId, attachmentFileIds);
        LambdaUpdateWrapper<PostDraft> updateWrapper = new LambdaUpdateWrapper<PostDraft>()
                .eq(PostDraft::getId, draft.getId())
                .set(PostDraft::getTitle, request.getTitle())
                .set(PostDraft::getContentMd, request.getContentMd())
                .set(PostDraft::getCoverUrl, request.getCoverUrl())
                .set(PostDraft::getTagIdsJson, writeIdList(tagIds))
                .set(PostDraft::getAttachmentFileIdsJson, writeIdList(attachmentFileIds));
        if (Boolean.TRUE.equals(request.getAutoSave())) {
            updateWrapper.set(PostDraft::getAutoSavedAt, LocalDateTime.now());
        }
        postDraftMapper.update(null, updateWrapper);
        return toDetailVo(postDraftMapper.selectById(draft.getId()), currentUserId);
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
        request.setTagIds(readIdList(draft.getTagIdsJson()));
        request.setAttachmentFileIds(readIdList(draft.getAttachmentFileIdsJson()));

        // 发布草稿本质上仍复用 PostService.create，避免草稿与正式发帖逻辑分叉。
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
        return toDetailVo(draft, draft.getUserId());
    }

    private DraftDetailVo toDetailVo(PostDraft draft, Long currentUserId) {
        List<Long> tagIds = readIdList(draft.getTagIdsJson());
        List<Long> attachmentFileIds = readIdList(draft.getAttachmentFileIdsJson());
        DraftDetailVo vo = new DraftDetailVo();
        vo.setId(draft.getId());
        vo.setTitle(draft.getTitle());
        vo.setContentMd(draft.getContentMd());
        vo.setCoverUrl(draft.getCoverUrl());
        vo.setTagIds(tagIds);
        vo.setTags(loadTags(tagIds));
        vo.setAttachmentFileIds(attachmentFileIds);
        vo.setAttachmentFiles(loadAttachmentFiles(currentUserId, attachmentFileIds));
        vo.setStatus(draft.getStatus());
        vo.setPublishedPostId(draft.getPublishedPostId());
        vo.setAutoSavedAt(draft.getAutoSavedAt());
        vo.setCreatedAt(draft.getCreatedAt());
        vo.setUpdatedAt(draft.getUpdatedAt());
        return vo;
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    private void validateTagIds(List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        long activeTagCount = tagMapper.selectCount(new LambdaQueryWrapper<Tag>()
                .in(Tag::getId, tagIds)
                .eq(Tag::getStatus, "ACTIVE"));
        if (activeTagCount != tagIds.size()) {
            throw BizException.of(ErrorCode.POST_TAG_INVALID);
        }
    }

    private void validateAttachmentFileIds(Long currentUserId, List<Long> attachmentFileIds) {
        if (CollectionUtils.isEmpty(attachmentFileIds)) {
            return;
        }
        long validFileCount = fileObjectMapper.selectCount(new LambdaQueryWrapper<FileObject>()
                .in(FileObject::getId, attachmentFileIds)
                .eq(FileObject::getUploaderId, currentUserId)
                .ne(FileObject::getStatus, "DELETED"));
        if (validFileCount != attachmentFileIds.size()) {
            throw BizException.of(ErrorCode.FILE_NOT_FOUND_OR_FORBIDDEN);
        }
    }

    private String writeIdList(List<Long> ids) {
        try {
            return objectMapper.writeValueAsString(ids == null ? Collections.emptyList() : ids);
        } catch (JsonProcessingException ex) {
            throw BizException.of(ErrorCode.SYSTEM_ERROR, "草稿数据序列化失败", ex);
        }
    }

    private List<Long> readIdList(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        try {
            Long[] ids = objectMapper.readValue(raw, Long[].class);
            if (ids == null || ids.length == 0) {
                return Collections.emptyList();
            }
            List<Long> result = new ArrayList<>(ids.length);
            for (Long id : ids) {
                if (id != null) {
                    result.add(id);
                }
            }
            return result;
        } catch (JsonProcessingException ex) {
            throw BizException.of(ErrorCode.SYSTEM_ERROR, "草稿数据解析失败", ex);
        }
    }

    private List<TagVo> loadTags(List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return Collections.emptyList();
        }
        Map<Long, TagVo> tagVoMap = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                        .in(Tag::getId, tagIds)
                        .eq(Tag::getStatus, "ACTIVE"))
                .stream()
                .collect(Collectors.toMap(Tag::getId, tag -> {
                    TagVo vo = new TagVo();
                    vo.setId(tag.getId());
                    vo.setName(tag.getName());
                    return vo;
                }, (a, b) -> a, LinkedHashMap::new));
        return tagIds.stream()
                .map(tagVoMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<FileObjectVo> loadAttachmentFiles(Long currentUserId, List<Long> attachmentFileIds) {
        if (CollectionUtils.isEmpty(attachmentFileIds)) {
            return Collections.emptyList();
        }
        Map<Long, FileObjectVo> fileVoMap = fileObjectMapper.selectList(new LambdaQueryWrapper<FileObject>()
                        .in(FileObject::getId, attachmentFileIds)
                        .eq(FileObject::getUploaderId, currentUserId)
                        .ne(FileObject::getStatus, "DELETED"))
                .stream()
                .collect(Collectors.toMap(FileObject::getId, file -> {
                    FileObjectVo vo = new FileObjectVo();
                    vo.setId(file.getId());
                    vo.setAccessUrl(file.getAccessUrl());
                    vo.setOriginalName(file.getOriginalName());
                    vo.setMimeType(file.getMimeType());
                    vo.setSizeBytes(file.getSizeBytes());
                    vo.setBizType(file.getBizType());
                    vo.setBizId(file.getBizId());
                    return vo;
                }, (a, b) -> a, LinkedHashMap::new));
        return attachmentFileIds.stream()
                .map(fileVoMap::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
