package com.community.backend.vo.notification;

import lombok.Data;

@Data
/**
 * 通知未读统计返回体。
 */
public class NotificationUnreadVo {

    /**
     * 全部未读消息总数。
     */
    private Integer total;
    /**
     * 帖子点赞类未读数量。
     */
    private Integer postLikeCount;
    /**
     * 评论类未读数量或评论总数。
     */
    private Integer commentCount;
    /**
     * 关注类未读数量。
     */
    private Integer followCount;
    /**
     * 私信聊天类未读数量。
     */
    private Integer chatCount;
    /**
     * 系统通知未读数量。
     */
    private Integer systemCount;
}
