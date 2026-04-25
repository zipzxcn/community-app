package com.community.backend.common.error;

import lombok.Getter;

/**
 * 统一错误码字典。
 * 说明：
 * 1) code=0 表示成功（见 ApiResponse.success）
 * 2) 非 0 均为失败，按模块分段管理
 */
@Getter
public enum ErrorCode {

    // ===== 通用（10xxx）=====
    SYSTEM_ERROR(10000, "服务器内部错误"),
    PARAM_VALIDATION_FAILED(10001, "请求参数校验失败"),
    REQUEST_BODY_INVALID(10002, "请求体格式不正确"),
    BUSINESS_BAD_REQUEST(10003, "请求不合法"),

    // ===== 鉴权（20xxx）=====
    AUTH_UNAUTHORIZED(20001, "未登录或令牌无效"),
    AUTH_ACCESS_DENIED(20003, "无权限访问"),
    AUTH_LOGIN_FAILED(20011, "用户名或密码错误"),
    AUTH_USERNAME_EXISTS(20012, "用户名已存在"),
    AUTH_ACCOUNT_STATUS_INVALID(20013, "账号状态不可用"),
    AUTH_REFRESH_TOKEN_INVALID(20014, "refreshToken 无效"),
    AUTH_REFRESH_TOKEN_EXPIRED(20015, "refreshToken 已过期"),

    // ===== 用户（21xxx）=====
    USER_NOT_FOUND(21001, "用户不存在"),
    USER_STATUS_INVALID(21002, "用户状态不可用"),
    USER_TARGET_NOT_FOUND(21003, "目标用户不存在"),

    // ===== 关注（22xxx）=====
    FOLLOW_SELF_NOT_ALLOWED(22001, "不能关注自己"),

    // ===== 帖子（23xxx）=====
    POST_NOT_FOUND(23001, "帖子不存在或不可见"),
    POST_TAG_INVALID(23002, "存在无效标签"),

    // ===== 评论（24xxx）=====
    COMMENT_NOT_FOUND(24001, "评论不存在或不可用"),
    COMMENT_CLOSED(24002, "该帖子已关闭评论"),
    COMMENT_DELETE_FORBIDDEN(24003, "无权限删除该评论"),
    COMMENT_PARENT_MISMATCH(24004, "路径评论ID与请求体不一致"),

    // ===== 聊天（25xxx）=====
    CHAT_SELF_NOT_ALLOWED(25001, "不能和自己发起会话"),
    CHAT_REQUIRE_MUTUAL_FOLLOW(25002, "仅互关用户可发起聊天"),
    CHAT_SEND_FORBIDDEN_BY_RELATION(25003, "当前非互关状态，无法发送消息"),
    CHAT_THREAD_NOT_FOUND(25004, "会话不存在"),
    CHAT_THREAD_FORBIDDEN(25005, "无权限访问该会话"),

    // ===== 文件/存储（26xxx）=====
    FILE_UPLOAD_TOKEN_FAILED(26001, "生成上传凭证失败"),
    FILE_BUCKET_MISMATCH(26002, "bucket 不匹配"),
    FILE_OBJECT_KEY_INVALID(26003, "objectKey 非当前用户上传路径"),
    FILE_NOT_OWNER(26004, "该文件不属于当前用户"),
    FILE_NOT_FOUND_OR_FORBIDDEN(26005, "文件不存在或无权限"),
    STORAGE_PROVIDER_UNSUPPORTED(26006, "当前 provider 未实现上传凭证生成"),
    STORAGE_MINIO_CLIENT_NOT_READY(26007, "MinIO 客户端未初始化"),
    STORAGE_BUCKET_NOT_CONFIGURED(26008, "app.storage.bucket 未配置"),
    STORAGE_CONFIG_INCOMPLETE(26009, "MinIO 配置不完整，请检查 app.storage.*"),
    FILE_OBJECT_NOT_FOUND(26010, "对象存储文件不存在"),
    FILE_SIZE_MISMATCH(26011, "对象存储文件大小不匹配"),

    // ===== 草稿（27xxx）=====
    DRAFT_NOT_FOUND(27001, "草稿不存在或不可用"),
    DRAFT_ALREADY_PUBLISHED(27002, "草稿已发布，不能重复发布"),
    DRAFT_CONTENT_EMPTY(27003, "草稿正文为空，无法发布"),

    // ===== 浏览历史（28xxx）=====
    HISTORY_NOT_FOUND(28001, "浏览历史不存在");

    private final Integer code;
    private final String defaultMessage;

    ErrorCode(Integer code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
