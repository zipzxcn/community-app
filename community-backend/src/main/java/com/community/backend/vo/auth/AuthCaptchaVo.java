package com.community.backend.vo.auth;

import lombok.Data;

@Data
public class AuthCaptchaVo {

    /**
     * 验证码唯一标识，提交登录/注册时需一并带回。
     */
    private String captchaId;
    /**
     * Base64 编码的验证码图片，前端可直接渲染到 img 标签。
     */
    private String captchaImageBase64;
    /**
     * 剩余有效秒数，前端可用于倒计时或失效提示。
     */
    private Long expireInSeconds;
}
