package com.community.backend.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 业务状态码，0 表示成功，非 0 表示失败。
     */
    private Integer code;
    /**
     * 接口响应描述，便于前端直接提示用户。
     */
    private String message;
    /**
     * 真正的业务载荷，成功时由各模块返回具体结构。
     */
    private T data;
    /**
     * 预留请求追踪 ID，方便接入网关或日志链路追踪。
     */
    private String requestId;

    /**
     * 成功响应：code 固定为 0。
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(0)
                .message("OK")
                .data(data)
                .build();
    }

    /**
     * 成功响应（自定义 message）：code 固定为 0。
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(0)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 失败响应：code 使用统一错误码字典（见 ErrorCode）。
     */
    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
}
