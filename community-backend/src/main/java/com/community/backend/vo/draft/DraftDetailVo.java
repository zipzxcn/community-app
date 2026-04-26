package com.community.backend.vo.draft;

import com.community.backend.vo.file.FileObjectVo;
import com.community.backend.vo.post.TagVo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
/**
 * 草稿详情返回体。
 */
public class DraftDetailVo {

    private Long id;
    private String title;
    private String contentMd;
    private String coverUrl;
    private List<Long> tagIds;
    private List<TagVo> tags;
    private List<Long> attachmentFileIds;
    private List<FileObjectVo> attachmentFiles;
    private String status;
    private Long publishedPostId;
    private LocalDateTime autoSavedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
