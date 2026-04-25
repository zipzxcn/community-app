package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 用户表 Mapper：用户基础信息与计数字段读写。
 */
public interface AppUserMapper extends BaseMapper<AppUser> {
}
