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
@TableName("post_tag_rel")
/**
 * 帖子与标签关联实体。
 */
public class PostTagRel {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 帖子 ID，关联帖子主表。
     */
    private Long postId;
    /**
     * 标签 ID。
     */
    private Long tagId;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
