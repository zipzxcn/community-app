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

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String refreshTokenHash;
    private String deviceType;
    private String deviceName;
    private String loginIp;
    private String userAgent;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
