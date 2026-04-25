package com.community.backend.vo.user;

import lombok.Data;

@Data
/**
 * 用户摘要信息返回体。
 */
public class UserSummaryVo {

    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
}
