-- 将文件访问地址统一切到后端代理，避免浏览器直接访问私有 MinIO 对象导致 AccessDenied。
UPDATE file_object
SET access_url = CONCAT('/api/v1/files/public/', id)
WHERE status <> 'DELETED'
  AND access_url NOT LIKE '/api/v1/files/public/%';

-- 修复用户头像。
UPDATE app_user u
JOIN file_object f
  ON u.avatar_url IS NOT NULL
 AND u.avatar_url LIKE CONCAT('%', f.object_key)
SET u.avatar_url = f.access_url
WHERE f.status <> 'DELETED';

-- 修复帖子封面。
UPDATE post p
JOIN file_object f
  ON p.cover_url IS NOT NULL
 AND p.cover_url LIKE CONCAT('%', f.object_key)
SET p.cover_url = f.access_url
WHERE f.status <> 'DELETED';

-- 修复草稿封面。
UPDATE post_draft d
JOIN file_object f
  ON d.cover_url IS NOT NULL
 AND d.cover_url LIKE CONCAT('%', f.object_key)
SET d.cover_url = f.access_url
WHERE f.status <> 'DELETED';
