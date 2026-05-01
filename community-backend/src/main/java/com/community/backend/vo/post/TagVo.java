package com.community.backend.vo.post;

import lombok.Data;

@Data
/**
 * 标签返回体。
 */
public class TagVo {

    /**
     * 标签 ID。
     */
    private Long id;
    /**
     * 名称字段，例如标签名。
     */
    private String name;
}
