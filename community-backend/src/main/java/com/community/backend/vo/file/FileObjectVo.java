package com.community.backend.vo.file;

import lombok.Data;

@Data
/**
 * 文件对象返回体。
 */
public class FileObjectVo {

    private Long id;
    private String accessUrl;
    private String originalName;
    private String mimeType;
    private Long sizeBytes;
    private String bizType;
    private Long bizId;
}
