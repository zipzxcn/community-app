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
     * 评论 ID。
     */
    private Long commentId;
    /**
     * 业务状态字段，不同对象有各自的枚举语义，例如 ACTIVE、PUBLISHED、HIDDEN。
     */
    private String status;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
}
