package com.community.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.backend.entity.FileObject;
import org.apache.ibatis.annotations.Mapper;

@Mapper
/**
 * 文件对象 Mapper：上传文件元数据持久化。
 */
public interface FileObjectMapper extends BaseMapper<FileObject> {
}
