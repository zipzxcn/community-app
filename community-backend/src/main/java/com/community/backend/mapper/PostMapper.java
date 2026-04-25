package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 帖子主表 Mapper。
 */
public interface PostMapper extends BaseMapper<Post> {
}
