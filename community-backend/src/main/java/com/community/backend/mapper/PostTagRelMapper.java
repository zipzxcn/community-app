package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.PostTagRel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 帖子-标签关系 Mapper。
 */
public interface PostTagRelMapper extends BaseMapper<PostTagRel> {
}
