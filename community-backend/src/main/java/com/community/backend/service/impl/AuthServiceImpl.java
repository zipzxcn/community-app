package com.community.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.backend.common.error.BizException;
import com.community.backend.common.error.ErrorCode;
import com.community.backend.common.util.TokenHashUtils;
import com.community.backend.dto.auth.LoginRequest;
import com.community.backend.dto.auth.RefreshTokenRequest;
import com.community.backend.dto.auth.RegisterRequest;
import com.community.backend.entity.AppUser;
import com.community.backend.entity.AuthRefreshSession;
import com.community.backend.mapper.AppUserMapper;
import com.community.backend.mapper.AuthRefreshSessionMapper;
import com.community.backend.security.JwtTokenProvider;
import com.community.backend.service.AuthService;
import com.community.backend.vo.auth.CurrentUserVo;
import com.community.backend.vo.auth.LoginVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 认证服务实现：处理注册、登录、刷新令牌与登出。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final AppUserMapper appUserMapper;
    private final AuthRefreshSessionMapper refreshSessionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.security.jwt.refresh-token-expire-seconds:1209600}")
    private long refreshTokenExpireSeconds;

    public AuthServiceImpl(AppUserMapper appUserMapper,
                           AuthRefreshSessionMapper refreshSessionMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.appUserMapper = appUserMapper;
        this.refreshSessionMapper = refreshSessionMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 注册账号：
     * 1) 校验用户名唯一
     * 2) 写入用户基础信息（密码使用 BCrypt）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        AppUser existed = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, request.getUsername())
                .eq(AppUser::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (existed != null) {
            throw BizException.of(ErrorCode.AUTH_USERNAME_EXISTS);
        }

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .status("ACTIVE")
                .postCount(0)
                .followerCount(0)
                .followingCount(0)
                .isDeleted(0)
                .build();
        appUserMapper.insert(user);
        return user.getId();
    }

    /**
     * 登录：
     * 1) 校验账号状态与密码
     * 2) 签发 access token
     * 3) 持久化 refresh token 会话
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVo login(LoginRequest request) {
        AppUser user = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getUsername, request.getUsername())
                .eq(AppUser::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (user == null || !StringUtils.hasText(user.getPasswordHash())
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw BizException.of(ErrorCode.AUTH_LOGIN_FAILED);
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw BizException.of(ErrorCode.AUTH_ACCOUNT_STATUS_INVALID);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername());
        String refreshToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String refreshTokenHash = TokenHashUtils.sha256(refreshToken);
        LocalDateTime now = LocalDateTime.now();

        AuthRefreshSession session = AuthRefreshSession.builder()
                .userId(user.getId())
                .refreshTokenHash(refreshTokenHash)
                .status("ACTIVE")
                .expiresAt(now.plusSeconds(refreshTokenExpireSeconds))
                .lastUsedAt(now)
                .build();
        refreshSessionMapper.insert(session);

        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, user.getId())
                .set(AppUser::getLastLoginAt, now));

        return buildLoginVo(user, accessToken, refreshToken);
    }

    /**
     * 获取当前登录用户信息。
     */
    @Override
    public CurrentUserVo me(Long userId) {
        AppUser user = mustGetActiveUser(userId);
        return toCurrentUserVo(user);
    }

    /**
     * 刷新令牌：
     * 1) 校验 refresh token 是否存在、有效、未过期
     * 2) 轮换 refresh token，降低泄漏风险
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVo refresh(RefreshTokenRequest request) {
        String hash = TokenHashUtils.sha256(request.getRefreshToken());
        AuthRefreshSession session = refreshSessionMapper.selectOne(new LambdaQueryWrapper<AuthRefreshSession>()
                .eq(AuthRefreshSession::getRefreshTokenHash, hash)
                .eq(AuthRefreshSession::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        if (session == null) {
            throw BizException.of(ErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }
        if (session.getExpiresAt() == null || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw BizException.of(ErrorCode.AUTH_REFRESH_TOKEN_EXPIRED);
        }

        AppUser user = mustGetActiveUser(session.getUserId());
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String newRefreshTokenHash = TokenHashUtils.sha256(newRefreshToken);
        LocalDateTime now = LocalDateTime.now();

        refreshSessionMapper.update(null, new LambdaUpdateWrapper<AuthRefreshSession>()
                .eq(AuthRefreshSession::getId, session.getId())
                .set(AuthRefreshSession::getRefreshTokenHash, newRefreshTokenHash)
                .set(AuthRefreshSession::getLastUsedAt, now)
                .set(AuthRefreshSession::getExpiresAt, now.plusSeconds(refreshTokenExpireSeconds)));

        return buildLoginVo(user, newAccessToken, newRefreshToken);
    }

    /**
     * 退出登录：撤销当前 refresh token 会话。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(Long userId, RefreshTokenRequest request) {
        String hash = TokenHashUtils.sha256(request.getRefreshToken());
        refreshSessionMapper.update(null, new LambdaUpdateWrapper<AuthRefreshSession>()
                .eq(AuthRefreshSession::getUserId, userId)
                .eq(AuthRefreshSession::getRefreshTokenHash, hash)
                .eq(AuthRefreshSession::getStatus, "ACTIVE")
                .set(AuthRefreshSession::getStatus, "REVOKED")
                .set(AuthRefreshSession::getRevokedAt, LocalDateTime.now()));
    }

    /**
     * 查询并校验有效用户。
     */
    private AppUser mustGetActiveUser(Long userId) {
        AppUser user = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getId, userId)
                .eq(AppUser::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (user == null) {
            throw BizException.of(ErrorCode.USER_NOT_FOUND);
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw BizException.of(ErrorCode.USER_STATUS_INVALID);
        }
        return user;
    }

    /**
     * 构建登录返回体。
     */
    private LoginVo buildLoginVo(AppUser user, String accessToken, String refreshToken) {
        LoginVo vo = new LoginVo();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(jwtTokenProvider.getAccessTokenExpireSeconds());
        vo.setUserInfo(toCurrentUserVo(user));
        return vo;
    }

    /**
     * 用户实体转当前用户 VO。
     */
    private CurrentUserVo toCurrentUserVo(AppUser user) {
        CurrentUserVo vo = new CurrentUserVo();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setBio(user.getBio());
        return vo;
    }
}
