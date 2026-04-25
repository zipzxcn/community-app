package com.community.backend.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
/**
 * 个人资料更新请求参数。
 */
public class UpdateProfileRequest {

    @Size(max = 32, message = "昵称长度不能超过32位")
    private String nickname;

    @Size(max = 255, message = "头像地址长度不能超过255位")
    private String avatarUrl;

    @Size(max = 255, message = "个人简介长度不能超过255位")
    private String bio;
}
