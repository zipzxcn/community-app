package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.PostFavorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 帖子收藏 Mapper。
 */
public interface PostFavoriteMapper extends BaseMapper<PostFavorite> {
}
