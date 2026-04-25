package com.community.backend.vo.notification;

import lombok.Data;

@Data
/**
 * 通知未读统计返回体。
 */
public class NotificationUnreadVo {

    private Integer total;
    private Integer postLikeCount;
    private Integer commentCount;
    private Integer followCount;
    private Integer chatCount;
    private Integer systemCount;
}
