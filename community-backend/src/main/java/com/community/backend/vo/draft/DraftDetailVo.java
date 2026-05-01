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

    /**
     * 草稿 ID。
     */
    private Long id;
    /**
     * 标题文本，用于帖子、通知等场景。
     */
    private String title;
    /**
     * Markdown 原始内容，便于再次编辑。
     */
    private String contentMd;
    /**
     * 封面图地址。
     */
    private String coverUrl;
    /**
     * 标签 ID 列表，用于发帖/草稿保存时关联标签。
     */
    private List<Long> tagIds;
    /**
     * 标签明细列表，前端展示时直接使用。
     */
    private List<TagVo> tags;
    /**
     * 附件文件 ID 列表，便于前后端只传引用。
     */
    private List<Long> attachmentFileIds;
    /**
     * 附件文件明细列表，前端可直接渲染。
     */
    private List<FileObjectVo> attachmentFiles;
    /**
     * 业务状态字段，不同对象有各自的枚举语义，例如 ACTIVE、PUBLISHED、HIDDEN。
     */
    private String status;
    /**
     * 草稿成功发布后关联的正式帖子 ID。
     */
    private Long publishedPostId;
    /**
     * 最近一次自动保存时间。
     */
    private LocalDateTime autoSavedAt;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
}
