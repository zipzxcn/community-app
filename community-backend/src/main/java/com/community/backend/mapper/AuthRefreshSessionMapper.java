package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.AuthRefreshSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 刷新令牌会话 Mapper：管理 refreshToken 生命周期。
 */
public interface AuthRefreshSessionMapper extends BaseMapper<AuthRefreshSession> {
}
