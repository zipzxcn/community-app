package com.community.backend.service;

import com.community.backend.dto.auth.LoginRequest;
import com.community.backend.dto.auth.RefreshTokenRequest;
import com.community.backend.dto.auth.RegisterRequest;
import com.community.backend.vo.auth.CurrentUserVo;
import com.community.backend.vo.auth.LoginVo;

public interface AuthService {

    Long register(RegisterRequest request);

    LoginVo login(LoginRequest request);

    CurrentUserVo me(Long userId);

    LoginVo refresh(RefreshTokenRequest request);

    void logout(Long userId, RefreshTokenRequest request);
}
