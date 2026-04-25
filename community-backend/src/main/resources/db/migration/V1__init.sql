-- 社区交流平台一期初始化脚本
-- 目标数据库: MySQL 8.0+
-- 建议字符集: utf8mb4
-- 说明:
-- 1. 该版本按单体应用设计, 适配 Spring Boot 3.x + MyBatis-Plus
-- 2. 该版本故意不加外键约束, 生产中通过应用服务层、唯一索引、状态字段保证一致性
-- 3. 如使用 Flyway, 可将本文件放到 src/main/resources/db/migration/V1__init.sql

CREATE TABLE IF NOT EXISTS app_user (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username            VARCHAR(32) NOT NULL COMMENT '用户名, 全站唯一',
    password_hash       VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    nickname            VARCHAR(32) NOT NULL COMMENT '昵称',
    avatar_url          VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    bio                 VARCHAR(255) DEFAULT NULL COMMENT '个人简介',
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/BANNED/DISABLED',
    post_count          INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '发帖数',
    follower_count      INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '粉丝数',
    following_count     INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '关注数',
    last_login_at       DATETIME(3) DEFAULT NULL COMMENT '最后登录时间',
    is_deleted          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否 1-是',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_username (username),
    KEY idx_app_user_status_created (status, created_at),
    KEY idx_app_user_nickname (nickname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS auth_refresh_session (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    refresh_token_hash  CHAR(64) NOT NULL COMMENT 'refresh token 的 sha256 哈希',
    device_type         VARCHAR(32) DEFAULT NULL COMMENT '设备类型: WEB/ANDROID/IOS',
    device_name         VARCHAR(64) DEFAULT NULL COMMENT '设备名称',
    login_ip            VARCHAR(64) DEFAULT NULL COMMENT '登录IP',
    user_agent          VARCHAR(255) DEFAULT NULL COMMENT '浏览器UA',
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/REVOKED/EXPIRED',
    expires_at          DATETIME(3) NOT NULL COMMENT '过期时间',
    last_used_at        DATETIME(3) DEFAULT NULL COMMENT '最后使用时间',
    revoked_at          DATETIME(3) DEFAULT NULL COMMENT '撤销时间',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_session_token_hash (refresh_token_hash),
    KEY idx_refresh_session_user_status_expire (user_id, status, expires_at),
    KEY idx_refresh_session_expire (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='刷新令牌会话表';

CREATE TABLE IF NOT EXISTS user_follow (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    follower_id         BIGINT UNSIGNED NOT NULL COMMENT '关注发起人ID',
    followee_id         BIGINT UNSIGNED NOT NULL COMMENT '被关注人ID',
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE',
    followed_at         DATETIME(3) DEFAULT NULL COMMENT '最近一次关注时间',
    unfollowed_at       DATETIME(3) DEFAULT NULL COMMENT '最近一次取关时间',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_follow_pair (follower_id, followee_id),
    KEY idx_user_follow_follower_status (follower_id, status, updated_at),
    KEY idx_user_follow_followee_status (followee_id, status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关注关系表';

CREATE TABLE IF NOT EXISTS tag (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name                VARCHAR(32) NOT NULL COMMENT '标签名',
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISABLED',
    post_count          INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '关联帖子数',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_name (name),
    KEY idx_tag_status_post_count (status, post_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';

CREATE TABLE IF NOT EXISTS post (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    author_id           BIGINT UNSIGNED NOT NULL COMMENT '作者ID',
    title               VARCHAR(120) NOT NULL COMMENT '标题',
    excerpt             VARCHAR(300) DEFAULT NULL COMMENT '摘要',
    content_md          MEDIUMTEXT COMMENT 'Markdown 原文',
    content_html        MEDIUMTEXT COMMENT '服务端清洗后的 HTML',
    cover_url           VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
    status              VARCHAR(16) NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态: PUBLISHED/HIDDEN',
    allow_comment       TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否允许评论',
    view_count          INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览数',
    like_count          INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
    favorite_count      INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
    comment_count       INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
    is_deleted          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否 1-是',
    published_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '发布时间',
    deleted_at          DATETIME(3) DEFAULT NULL COMMENT '删除时间',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_post_status_deleted_publish (status, is_deleted, published_at),
    KEY idx_post_author_deleted_publish (author_id, is_deleted, published_at),
    KEY idx_post_title (title),
    KEY idx_post_hot_sort (status, is_deleted, like_count, comment_count, published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子表';

CREATE TABLE IF NOT EXISTS post_draft (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '草稿所属用户ID',
    title               VARCHAR(120) NOT NULL COMMENT '草稿标题',
    content_md          MEDIUMTEXT COMMENT '草稿正文Markdown',
    cover_url           VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISCARDED/PUBLISHED',
    published_post_id   BIGINT UNSIGNED DEFAULT NULL COMMENT '若已发布则指向帖子ID',
    auto_saved_at       DATETIME(3) DEFAULT NULL COMMENT '最近自动保存时间',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_post_draft_user_status_updated (user_id, status, updated_at),
    KEY idx_post_draft_published_post (published_post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子草稿表';

CREATE TABLE IF NOT EXISTS post_tag_rel (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    post_id             BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
    tag_id              BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_tag_rel_pair (post_id, tag_id),
    KEY idx_post_tag_rel_tag_post (tag_id, post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子标签关联表';

CREATE TABLE IF NOT EXISTS file_object (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    uploader_id         BIGINT UNSIGNED NOT NULL COMMENT '上传人ID',
    storage_provider    VARCHAR(16) NOT NULL DEFAULT 'MINIO' COMMENT '存储类型: MINIO/OSS/S3/COS',
    bucket_name         VARCHAR(64) NOT NULL COMMENT '桶名',
    object_key          VARCHAR(255) NOT NULL COMMENT '对象存储Key',
    access_url          VARCHAR(255) NOT NULL COMMENT '访问URL',
    original_name       VARCHAR(255) NOT NULL COMMENT '原文件名',
    ext                 VARCHAR(16) DEFAULT NULL COMMENT '扩展名',
    mime_type           VARCHAR(128) NOT NULL COMMENT 'MIME 类型',
    size_bytes          BIGINT UNSIGNED NOT NULL COMMENT '文件大小(字节)',
    width               INT UNSIGNED DEFAULT NULL COMMENT '图片宽度',
    height              INT UNSIGNED DEFAULT NULL COMMENT '图片高度',
    checksum_md5        CHAR(32) DEFAULT NULL COMMENT 'MD5 校验值',
    biz_type            VARCHAR(32) DEFAULT NULL COMMENT '业务类型: AVATAR/POST/POST_DRAFT/CHAT',
    biz_id              BIGINT UNSIGNED DEFAULT NULL COMMENT '业务主键ID',
    sort_order          INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '展示顺序',
    status              VARCHAR(16) NOT NULL DEFAULT 'UPLOADED' COMMENT '状态: UPLOADED/BOUND/DELETED',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_object_bucket_key (bucket_name, object_key),
    KEY idx_file_object_uploader_created (uploader_id, created_at),
    KEY idx_file_object_biz (biz_type, biz_id, sort_order),
    KEY idx_file_object_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件元数据表';

CREATE TABLE IF NOT EXISTS post_like (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '点赞用户ID',
    post_id             BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/CANCELLED',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '首次点赞时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后操作时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_like_user_post (user_id, post_id),
    KEY idx_post_like_post_status (post_id, status, updated_at),
    KEY idx_post_like_user_status (user_id, status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子点赞表';

CREATE TABLE IF NOT EXISTS post_favorite (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '收藏用户ID',
    post_id             BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/CANCELLED',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '首次收藏时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后操作时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_favorite_user_post (user_id, post_id),
    KEY idx_post_favorite_post_status (post_id, status, updated_at),
    KEY idx_post_favorite_user_status (user_id, status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子收藏表';

CREATE TABLE IF NOT EXISTS post_comment (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    post_id             BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '评论人ID',
    parent_id           BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父评论ID, 一级评论为0',
    root_id             BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '根评论ID, 一级评论为0',
    reply_to_user_id    BIGINT UNSIGNED DEFAULT NULL COMMENT '被回复用户ID',
    content             TEXT NOT NULL COMMENT '评论内容',
    like_count          INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
    reply_count         INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '回复数',
    status              VARCHAR(32) NOT NULL DEFAULT 'NORMAL' COMMENT '状态: NORMAL/DELETED_BY_AUTHOR/DELETED_BY_POST_OWNER/HIDDEN',
    deleted_at          DATETIME(3) DEFAULT NULL COMMENT '删除时间',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_post_comment_post_root_created (post_id, root_id, status, created_at),
    KEY idx_post_comment_parent_created (parent_id, created_at),
    KEY idx_post_comment_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子评论表';

CREATE TABLE IF NOT EXISTS comment_like (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '点赞用户ID',
    comment_id          BIGINT UNSIGNED NOT NULL COMMENT '评论ID',
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/CANCELLED',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '首次点赞时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后操作时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_comment_like_user_comment (user_id, comment_id),
    KEY idx_comment_like_comment_status (comment_id, status, updated_at),
    KEY idx_comment_like_user_status (user_id, status, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论点赞表';

CREATE TABLE IF NOT EXISTS user_notification (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    receiver_id         BIGINT UNSIGNED NOT NULL COMMENT '接收用户ID',
    actor_id            BIGINT UNSIGNED DEFAULT NULL COMMENT '触发行为的用户ID, 系统通知可为空',
    type                VARCHAR(16) NOT NULL COMMENT '类型: POST_LIKE/COMMENT/FOLLOW/CHAT/SYSTEM',
    target_type         VARCHAR(32) DEFAULT NULL COMMENT '目标类型: POST/COMMENT/THREAD/USER',
    target_id           BIGINT UNSIGNED DEFAULT NULL COMMENT '目标ID',
    title               VARCHAR(120) NOT NULL COMMENT '通知标题',
    content             VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
    extra_json          JSON DEFAULT NULL COMMENT '扩展字段',
    is_read             TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读: 0-否 1-是',
    read_at             DATETIME(3) DEFAULT NULL COMMENT '已读时间',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_notification_receiver_read_created (receiver_id, is_read, created_at),
    KEY idx_user_notification_receiver_type_read_created (receiver_id, type, is_read, created_at),
    KEY idx_user_notification_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户通知表';

CREATE TABLE IF NOT EXISTS user_browse_history (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    post_id             BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
    view_count          INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '浏览次数',
    last_viewed_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '最后浏览时间',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_browse_history_user_post (user_id, post_id),
    KEY idx_user_browse_history_user_last_viewed (user_id, last_viewed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户浏览历史表';

CREATE TABLE IF NOT EXISTS chat_thread (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_a_id           BIGINT UNSIGNED NOT NULL COMMENT '会话用户A, 约定存较小ID',
    user_b_id           BIGINT UNSIGNED NOT NULL COMMENT '会话用户B, 约定存较大ID',
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/READ_ONLY',
    last_message_id     BIGINT UNSIGNED DEFAULT NULL COMMENT '最后一条消息ID',
    last_message_preview VARCHAR(200) DEFAULT NULL COMMENT '最后一条消息摘要',
    last_message_at     DATETIME(3) DEFAULT NULL COMMENT '最后消息时间',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_chat_thread_user_pair (user_a_id, user_b_id),
    KEY idx_chat_thread_status_last_message_at (status, last_message_at),
    KEY idx_chat_thread_user_a (user_a_id),
    KEY idx_chat_thread_user_b (user_b_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='私聊会话表';

CREATE TABLE IF NOT EXISTS chat_thread_user (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    thread_id           BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
    user_id             BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    unread_count        INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '未读数',
    last_read_message_id BIGINT UNSIGNED DEFAULT NULL COMMENT '最后已读消息ID',
    last_read_at        DATETIME(3) DEFAULT NULL COMMENT '最后已读时间',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_chat_thread_user_pair (thread_id, user_id),
    KEY idx_chat_thread_user_user_unread (user_id, unread_count, updated_at),
    KEY idx_chat_thread_user_thread (thread_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会话-用户状态表';

CREATE TABLE IF NOT EXISTS chat_message (
    id                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    thread_id           BIGINT UNSIGNED NOT NULL COMMENT '会话ID',
    sender_id           BIGINT UNSIGNED NOT NULL COMMENT '发送者ID',
    receiver_id         BIGINT UNSIGNED NOT NULL COMMENT '接收者ID',
    client_msg_id       VARCHAR(64) NOT NULL COMMENT '客户端消息ID, 用于幂等去重',
    message_type        VARCHAR(16) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型: TEXT/IMAGE/SYSTEM',
    content             TEXT NOT NULL COMMENT '消息内容, 文本或结构化JSON字符串',
    extra_json          JSON DEFAULT NULL COMMENT '扩展字段',
    is_read             TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读: 0-否 1-是',
    read_at             DATETIME(3) DEFAULT NULL COMMENT '已读时间',
    status              VARCHAR(16) NOT NULL DEFAULT 'SENT' COMMENT '状态: SENT/READ/DELETED',
    created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_chat_message_sender_client_msg (sender_id, client_msg_id),
    KEY idx_chat_message_thread_created (thread_id, created_at),
    KEY idx_chat_message_receiver_read_thread (receiver_id, is_read, thread_id, created_at),
    KEY idx_chat_message_sender_created (sender_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息表';
