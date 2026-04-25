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
@TableName("comment_like")
/**
 * 评论点赞关系实体。
 */
public class CommentLike {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long commentId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
