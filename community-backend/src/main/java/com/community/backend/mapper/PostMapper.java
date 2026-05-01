package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.Post;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子主表 Mapper：
 * - 帖子列表、详情、编辑、删除等核心查询最终都会落到这里。
 * - 结合 MyBatis-Plus Wrapper，可快速拼装筛选、排序、分页条件。
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
