package com.community.backend.vo.file;

import java.io.InputStream;

/**
 * 文件下载结果：供公开文件代理接口使用。
 */
public record FileDownloadVo(
        String contentType,
        Long contentLength,
        String originalName,
        InputStream inputStream
) {
}
