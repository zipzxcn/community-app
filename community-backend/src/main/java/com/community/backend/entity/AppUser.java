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
@TableName("app_user")
/**
 * 用户实体：系统账号、资料与统计计数。
 */
public class AppUser {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 登录用户名/账号标识，系统内要求唯一。
     */
    private String username;
    /**
     * 使用 Spring Security BCryptPasswordEncoder 加密后的密码摘要，不存储明文。
     */
    private String passwordHash;
    /**
     * 用户昵称，用于前台展示。
     */
    private String nickname;
    /**
     * 头像访问地址，通常指向对象存储公开代理地址。
     */
    private String avatarUrl;
    /**
     * 个人简介，用于用户主页展示。
     */
    private String bio;
    /**
     * 业务状态字段，不同对象有各自的枚举语义，例如 ACTIVE、PUBLISHED、HIDDEN。
     */
    private String status;
    /**
     * 用户已发布帖子数或标签关联帖子数等统计值。
     */
    private Integer postCount;
    /**
     * 粉丝数量统计。
     */
    private Integer followerCount;
    /**
     * 关注数量统计。
     */
    private Integer followingCount;
    /**
     * 最后一次成功登录时间。
     */
    private LocalDateTime lastLoginAt;
    /**
     * 逻辑删除标记，0 表示未删除，1 表示已删除。
     */
    private Integer isDeleted;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
}
