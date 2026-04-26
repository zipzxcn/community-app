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

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String contentMd;
    private String coverUrl;
    private String tagIdsJson;
    private String attachmentFileIdsJson;
    private String status;
    private Long publishedPostId;
    private LocalDateTime autoSavedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
