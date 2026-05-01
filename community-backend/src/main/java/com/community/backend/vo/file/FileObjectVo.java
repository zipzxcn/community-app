package com.community.backend.vo.file;

import lombok.Data;

@Data
/**
 * 文件对象返回体。
 */
public class FileObjectVo {

    /**
     * 主键 ID。
     */
    private Long id;
    /**
     * 文件访问地址，前端通常直接持有该地址用于展示。
     */
    private String accessUrl;
    /**
     * 原始文件名，便于前端展示给用户。
     */
    private String originalName;
    /**
     * 文件 MIME 类型，用于浏览器展示或下载。
     */
    private String mimeType;
    /**
     * 文件大小，单位字节。
     */
    private Long sizeBytes;
    /**
     * 业务场景类型，例如 avatar、post-cover、chat-image。
     */
    private String bizType;
    /**
     * 业务场景关联主键，例如帖子 ID、用户 ID。
     */
    private Long bizId;
}
