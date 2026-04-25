package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.UserBrowseHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 用户浏览历史 Mapper。
 */
public interface UserBrowseHistoryMapper extends BaseMapper<UserBrowseHistory> {
}
