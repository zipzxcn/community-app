package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 标签 Mapper。
 */
public interface TagMapper extends BaseMapper<Tag> {
}
