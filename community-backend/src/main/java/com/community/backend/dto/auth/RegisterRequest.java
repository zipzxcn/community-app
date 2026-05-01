package com.community.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * 注册请求参数：用户名、密码、昵称。
 */
public class RegisterRequest {

    /**
     * 登录用户名/账号标识，系统内要求唯一。
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度必须在3到32位之间")
    private String username;

    /**
     * 登录或注册时提交的明文密码，进入服务层后会做 BCrypt 校验或加密。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6到32位之间")
    private String password;

    /**
     * 用户昵称，用于前台展示。
     */
    @NotBlank(message = "昵称不能为空")
    @Size(max = 32, message = "昵称长度不能超过32位")
    private String nickname;

    /**
     * 图形验证码唯一标识，服务端据此校验验证码答案。
     */
    @NotBlank(message = "验证码标识不能为空")
    private String captchaId;

    /**
     * 用户输入的图形验证码内容。
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 8, message = "验证码长度不正确")
    private String captchaCode;
}
