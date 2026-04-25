package com.community.backend.security;

import com.community.backend.common.error.BizException;
import com.community.backend.common.error.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具：统一读取当前登录用户信息。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 读取当前 LoginUser，匿名请求返回 null。
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return null;
        }
        return loginUser;
    }

    /**
     * 获取当前用户 ID（可为空，适合游客可访问接口）。
     */
    public static Long getCurrentUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser == null ? null : loginUser.getUserId();
    }

    /**
     * 强制要求登录，未登录直接抛错。
     */
    public static Long requireUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw BizException.of(ErrorCode.AUTH_UNAUTHORIZED);
        }
        return userId;
    }
}
