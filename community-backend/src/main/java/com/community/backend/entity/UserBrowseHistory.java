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
@TableName("user_browse_history")
/**
 * 用户浏览历史实体。
 */
public class UserBrowseHistory {

    /**
     * 浏览历史主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 当前业务记录所属的用户 ID。
     */
    private Long userId;
    /**
     * 帖子 ID，关联帖子主表。
     */
    private Long postId;
    /**
     * 浏览次数统计。
     */
    private Integer viewCount;
    private LocalDateTime lastViewedAt;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
}
