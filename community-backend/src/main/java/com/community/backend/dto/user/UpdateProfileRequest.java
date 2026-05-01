package com.community.backend.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * 个人资料更新请求参数。
 */
public class UpdateProfileRequest {

    /**
     * 用户昵称，用于前台展示。
     */
    @Size(max = 32, message = "昵称长度不能超过32位")
    private String nickname;

    /**
     * 头像访问地址，通常指向对象存储公开代理地址。
     */
    @Size(max = 255, message = "头像地址长度不能超过255位")
    private String avatarUrl;

    /**
     * 个人简介，用于用户主页展示。
     */
    @Size(max = 255, message = "个人简介长度不能超过255位")
    private String bio;
}
