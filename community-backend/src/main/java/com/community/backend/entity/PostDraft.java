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
@TableName("post_draft")
/**
 * 帖子草稿实体。
 */
public class PostDraft {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 当前业务记录所属的用户 ID。
     */
    private Long userId;
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
     * 草稿中标签 ID 列表的 JSON 串，便于草稿整体存档。
     */
    private String tagIdsJson;
    /**
     * 草稿中附件文件 ID 列表的 JSON 串。
     */
    private String attachmentFileIdsJson;
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
