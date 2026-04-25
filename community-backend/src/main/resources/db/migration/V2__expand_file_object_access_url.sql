-- 将 access_url 扩容到 2048，兼容 MinIO 预签名 URL
ALTER TABLE file_object
    MODIFY COLUMN access_url VARCHAR(2048) NOT NULL COMMENT '访问URL';

