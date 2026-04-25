package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 帖子评论 Mapper。
 */
public interface PostCommentMapper extends BaseMapper<PostComment> {
}
