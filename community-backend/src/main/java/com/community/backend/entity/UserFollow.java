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
@TableName("user_follow")
/**
 * 用户关注关系实体。
 */
public class UserFollow {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long followerId;
    private Long followeeId;
    private String status;
    private LocalDateTime followedAt;
    private LocalDateTime unfollowedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
