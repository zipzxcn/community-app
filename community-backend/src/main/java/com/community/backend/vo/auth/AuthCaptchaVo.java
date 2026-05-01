package com.community.backend.vo.auth;

import lombok.Data;

@Data
public class AuthCaptchaVo {

    private String captchaId;
    private String captchaSvg;
    private Long expireInSeconds;
}
