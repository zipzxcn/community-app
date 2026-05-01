package com.community.backend.service;

import com.community.backend.vo.auth.AuthCaptchaVo;

public interface CaptchaService {

    AuthCaptchaVo create();

    void validate(String captchaId, String captchaCode);
}
