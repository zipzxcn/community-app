/*
 Navicat Premium Dump SQL

 Source Server         : localhost-3306
 Source Server Type    : MySQL
 Source Server Version : 80408 (8.4.8)
 Source Host           : localhost:3306
 Source Schema         : community_app

 Target Server Type    : MySQL
 Target Server Version : 80408 (8.4.8)
 File Encoding         : 65001

 Date: 02/05/2026 15:18:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app_user
-- ----------------------------
DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名, 全站唯一',
  `password_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'BCrypt 密码哈希',
  `nickname` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `bio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/BANNED/DISABLED',
  `post_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '发帖数',
  `follower_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `following_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '关注数',
  `last_login_at` datetime(3) NULL DEFAULT NULL COMMENT '最后登录时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否 1-是',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_app_user_username`(`username` ASC) USING BTREE,
  INDEX `idx_app_user_status_created`(`status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_app_user_nickname`(`nickname` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 434 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for auth_refresh_session
-- ----------------------------
DROP TABLE IF EXISTS `auth_refresh_session`;
CREATE TABLE `auth_refresh_session`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `refresh_token_hash` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'refresh token 的 sha256 哈希',
  `device_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备类型: WEB/ANDROID/IOS',
  `device_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备名称',
  `login_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录IP',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器UA',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/REVOKED/EXPIRED',
  `expires_at` datetime(3) NOT NULL COMMENT '过期时间',
  `last_used_at` datetime(3) NULL DEFAULT NULL COMMENT '最后使用时间',
  `revoked_at` datetime(3) NULL DEFAULT NULL COMMENT '撤销时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_refresh_session_token_hash`(`refresh_token_hash` ASC) USING BTREE,
  INDEX `idx_refresh_session_user_status_expire`(`user_id` ASC, `status` ASC, `expires_at` ASC) USING BTREE,
  INDEX `idx_refresh_session_expire`(`expires_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 466 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '刷新令牌会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `thread_id` bigint UNSIGNED NOT NULL COMMENT '会话ID',
  `sender_id` bigint UNSIGNED NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint UNSIGNED NOT NULL COMMENT '接收者ID',
  `client_msg_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端消息ID, 用于幂等去重',
  `message_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'TEXT' COMMENT '消息类型: TEXT/IMAGE/SYSTEM',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容, 文本或结构化JSON字符串',
  `extra_json` json NULL COMMENT '扩展字段',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读: 0-否 1-是',
  `read_at` datetime(3) NULL DEFAULT NULL COMMENT '已读时间',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SENT' COMMENT '状态: SENT/READ/DELETED',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_chat_message_sender_client_msg`(`sender_id` ASC, `client_msg_id` ASC) USING BTREE,
  INDEX `idx_chat_message_thread_created`(`thread_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_chat_message_receiver_read_thread`(`receiver_id` ASC, `is_read` ASC, `thread_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_chat_message_sender_created`(`sender_id` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_thread
-- ----------------------------
DROP TABLE IF EXISTS `chat_thread`;
CREATE TABLE `chat_thread`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_a_id` bigint UNSIGNED NOT NULL COMMENT '会话用户A, 约定存较小ID',
  `user_b_id` bigint UNSIGNED NOT NULL COMMENT '会话用户B, 约定存较大ID',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/READ_ONLY',
  `last_message_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_preview` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后一条消息摘要',
  `last_message_at` datetime(3) NULL DEFAULT NULL COMMENT '最后消息时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_chat_thread_user_pair`(`user_a_id` ASC, `user_b_id` ASC) USING BTREE,
  INDEX `idx_chat_thread_status_last_message_at`(`status` ASC, `last_message_at` ASC) USING BTREE,
  INDEX `idx_chat_thread_user_a`(`user_a_id` ASC) USING BTREE,
  INDEX `idx_chat_thread_user_b`(`user_b_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '私聊会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for chat_thread_user
-- ----------------------------
DROP TABLE IF EXISTS `chat_thread_user`;
CREATE TABLE `chat_thread_user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `thread_id` bigint UNSIGNED NOT NULL COMMENT '会话ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `unread_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '未读数',
  `last_read_message_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '最后已读消息ID',
  `last_read_at` datetime(3) NULL DEFAULT NULL COMMENT '最后已读时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_chat_thread_user_pair`(`thread_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_chat_thread_user_user_unread`(`user_id` ASC, `unread_count` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_chat_thread_user_thread`(`thread_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '会话-用户状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for comment_like
-- ----------------------------
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '点赞用户ID',
  `comment_id` bigint UNSIGNED NOT NULL COMMENT '评论ID',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/CANCELLED',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '首次点赞时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_comment_like_user_comment`(`user_id` ASC, `comment_id` ASC) USING BTREE,
  INDEX `idx_comment_like_comment_status`(`comment_id` ASC, `status` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_comment_like_user_status`(`user_id` ASC, `status` ASC, `updated_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for file_object
-- ----------------------------
DROP TABLE IF EXISTS `file_object`;
CREATE TABLE `file_object`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `uploader_id` bigint UNSIGNED NOT NULL COMMENT '上传人ID',
  `storage_provider` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MINIO' COMMENT '存储类型: MINIO/OSS/S3/COS',
  `bucket_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '桶名',
  `object_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '对象存储Key',
  `access_url` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '访问URL',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原文件名',
  `ext` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展名',
  `mime_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'MIME 类型',
  `size_bytes` bigint UNSIGNED NOT NULL COMMENT '文件大小(字节)',
  `width` int UNSIGNED NULL DEFAULT NULL COMMENT '图片宽度',
  `height` int UNSIGNED NULL DEFAULT NULL COMMENT '图片高度',
  `checksum_md5` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'MD5 校验值',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型: AVATAR/POST/POST_DRAFT/CHAT',
  `biz_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '业务主键ID',
  `sort_order` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '展示顺序',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'UPLOADED' COMMENT '状态: UPLOADED/BOUND/DELETED',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_file_object_bucket_key`(`bucket_name` ASC, `object_key` ASC) USING BTREE,
  INDEX `idx_file_object_uploader_created`(`uploader_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_file_object_biz`(`biz_type` ASC, `biz_id` ASC, `sort_order` ASC) USING BTREE,
  INDEX `idx_file_object_status_created`(`status` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件元数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for flyway_schema_history
-- ----------------------------
DROP TABLE IF EXISTS `flyway_schema_history`;
CREATE TABLE `flyway_schema_history`  (
  `installed_rank` int NOT NULL,
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `script` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `checksum` int NULL DEFAULT NULL,
  `installed_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`) USING BTREE,
  INDEX `flyway_schema_history_s_idx`(`success` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `author_id` bigint UNSIGNED NOT NULL COMMENT '作者ID',
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `excerpt` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '摘要',
  `content_md` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'Markdown 原文',
  `content_html` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '服务端清洗后的 HTML',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态: PUBLISHED/HIDDEN',
  `allow_comment` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许评论',
  `view_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览数',
  `like_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `favorite_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
  `comment_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-否 1-是',
  `published_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '发布时间',
  `deleted_at` datetime(3) NULL DEFAULT NULL COMMENT '删除时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_status_deleted_publish`(`status` ASC, `is_deleted` ASC, `published_at` ASC) USING BTREE,
  INDEX `idx_post_author_deleted_publish`(`author_id` ASC, `is_deleted` ASC, `published_at` ASC) USING BTREE,
  INDEX `idx_post_title`(`title` ASC) USING BTREE,
  INDEX `idx_post_hot_sort`(`status` ASC, `is_deleted` ASC, `like_count` ASC, `comment_count` ASC, `published_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 204 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_comment
-- ----------------------------
DROP TABLE IF EXISTS `post_comment`;
CREATE TABLE `post_comment`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint UNSIGNED NOT NULL COMMENT '帖子ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '评论人ID',
  `parent_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '父评论ID, 一级评论为0',
  `root_id` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT '根评论ID, 一级评论为0',
  `reply_to_user_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '被回复用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `like_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '回复数',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NORMAL' COMMENT '状态: NORMAL/DELETED_BY_AUTHOR/DELETED_BY_POST_OWNER/HIDDEN',
  `deleted_at` datetime(3) NULL DEFAULT NULL COMMENT '删除时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_comment_post_root_created`(`post_id` ASC, `root_id` ASC, `status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_post_comment_parent_created`(`parent_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_post_comment_user_created`(`user_id` ASC, `created_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 117 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_draft
-- ----------------------------
DROP TABLE IF EXISTS `post_draft`;
CREATE TABLE `post_draft`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '草稿所属用户ID',
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '草稿标题',
  `content_md` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '草稿正文Markdown',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图URL',
  `tag_ids_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '草稿标签ID列表(JSON)',
  `attachment_file_ids_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '草稿附件文件ID列表(JSON)',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISCARDED/PUBLISHED',
  `published_post_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '若已发布则指向帖子ID',
  `auto_saved_at` datetime(3) NULL DEFAULT NULL COMMENT '最近自动保存时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_draft_user_status_updated`(`user_id` ASC, `status` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_post_draft_published_post`(`published_post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 68 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子草稿表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_favorite
-- ----------------------------
DROP TABLE IF EXISTS `post_favorite`;
CREATE TABLE `post_favorite`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '收藏用户ID',
  `post_id` bigint UNSIGNED NOT NULL COMMENT '帖子ID',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/CANCELLED',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '首次收藏时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_favorite_user_post`(`user_id` ASC, `post_id` ASC) USING BTREE,
  INDEX `idx_post_favorite_post_status`(`post_id` ASC, `status` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_post_favorite_user_status`(`user_id` ASC, `status` ASC, `updated_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 57 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_like
-- ----------------------------
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '点赞用户ID',
  `post_id` bigint UNSIGNED NOT NULL COMMENT '帖子ID',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/CANCELLED',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '首次点赞时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '最后操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_like_user_post`(`user_id` ASC, `post_id` ASC) USING BTREE,
  INDEX `idx_post_like_post_status`(`post_id` ASC, `status` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_post_like_user_status`(`user_id` ASC, `status` ASC, `updated_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 105 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_tag_rel
-- ----------------------------
DROP TABLE IF EXISTS `post_tag_rel`;
CREATE TABLE `post_tag_rel`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` bigint UNSIGNED NOT NULL COMMENT '帖子ID',
  `tag_id` bigint UNSIGNED NOT NULL COMMENT '标签ID',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_tag_rel_pair`(`post_id` ASC, `tag_id` ASC) USING BTREE,
  INDEX `idx_post_tag_rel_tag_post`(`tag_id` ASC, `post_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/DISABLED',
  `post_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '关联帖子数',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tag_name`(`name` ASC) USING BTREE,
  INDEX `idx_tag_status_post_count`(`status` ASC, `post_count` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_browse_history
-- ----------------------------
DROP TABLE IF EXISTS `user_browse_history`;
CREATE TABLE `user_browse_history`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `post_id` bigint UNSIGNED NOT NULL COMMENT '帖子ID',
  `view_count` int UNSIGNED NOT NULL DEFAULT 1 COMMENT '浏览次数',
  `last_viewed_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '最后浏览时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_browse_history_user_post`(`user_id` ASC, `post_id` ASC) USING BTREE,
  INDEX `idx_user_browse_history_user_last_viewed`(`user_id` ASC, `last_viewed_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户浏览历史表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_follow
-- ----------------------------
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `follower_id` bigint UNSIGNED NOT NULL COMMENT '关注发起人ID',
  `followee_id` bigint UNSIGNED NOT NULL COMMENT '被关注人ID',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE',
  `followed_at` datetime(3) NULL DEFAULT NULL COMMENT '最近一次关注时间',
  `unfollowed_at` datetime(3) NULL DEFAULT NULL COMMENT '最近一次取关时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_follow_pair`(`follower_id` ASC, `followee_id` ASC) USING BTREE,
  INDEX `idx_user_follow_follower_status`(`follower_id` ASC, `status` ASC, `updated_at` ASC) USING BTREE,
  INDEX `idx_user_follow_followee_status`(`followee_id` ASC, `status` ASC, `updated_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 131 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '关注关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_notification
-- ----------------------------
DROP TABLE IF EXISTS `user_notification`;
CREATE TABLE `user_notification`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receiver_id` bigint UNSIGNED NOT NULL COMMENT '接收用户ID',
  `actor_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '触发行为的用户ID, 系统通知可为空',
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型: POST_LIKE/COMMENT/FOLLOW/CHAT/SYSTEM',
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标类型: POST/COMMENT/THREAD/USER',
  `target_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '目标ID',
  `title` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通知内容',
  `extra_json` json NULL COMMENT '扩展字段',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读: 0-否 1-是',
  `read_at` datetime(3) NULL DEFAULT NULL COMMENT '已读时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_notification_receiver_read_created`(`receiver_id` ASC, `is_read` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_user_notification_receiver_type_read_created`(`receiver_id` ASC, `type` ASC, `is_read` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_user_notification_target`(`target_type` ASC, `target_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 369 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户通知表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
