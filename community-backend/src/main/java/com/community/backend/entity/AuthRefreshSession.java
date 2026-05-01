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
@TableName("auth_refresh_session")
/**
 * 刷新令牌会话实体：记录 refreshToken 哈希与状态。
 */
public class AuthRefreshSession {

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
     * refreshToken 的 SHA-256 摘要，数据库只保存哈希值以降低泄漏风险。
     */
    private String refreshTokenHash;
    /**
     * 登录设备类型，例如 WEB、APP。
     */
    private String deviceType;
    /**
     * 设备名称或浏览器标识，便于后续扩展登录设备管理。
     */
    private String deviceName;
    /**
     * 登录时记录的客户端 IP。
     */
    private String loginIp;
    /**
     * 浏览器或客户端 User-Agent 信息。
     */
    private String userAgent;
    /**
     * 业务状态字段，不同对象有各自的枚举语义，例如 ACTIVE、PUBLISHED、HIDDEN。
     */
    private String status;
    /**
     * 过期时间，超过后即使状态仍为 ACTIVE 也不可继续使用。
     */
    private LocalDateTime expiresAt;
    /**
     * 最近一次使用时间，用于刷新令牌轮换审计。
     */
    private LocalDateTime lastUsedAt;
    /**
     * 撤销时间，通常用于登出或强制下线。
     */
    private LocalDateTime revokedAt;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
}
