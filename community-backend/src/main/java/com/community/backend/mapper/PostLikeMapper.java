package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 帖子点赞 Mapper。
 */
public interface PostLikeMapper extends BaseMapper<PostLike> {
}
