package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper：
 * - 继承 MyBatis-Plus BaseMapper 后即可获得通用 CRUD。
 * - 主要服务于认证、用户中心、关系统计等用户基础信息读写场景。
 */
@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
}
