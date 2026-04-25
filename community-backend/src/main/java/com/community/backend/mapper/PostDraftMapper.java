package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.PostDraft;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 帖子草稿 Mapper。
 */
public interface PostDraftMapper extends BaseMapper<PostDraft> {
}
