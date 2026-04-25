package com.community.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_notification")
/**
 * 用户通知实体。
 */
public class UserNotification {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long receiverId;
    private Long actorId;
    private String type;
    private String targetType;
    private Long targetId;
    private String title;
    private String content;
    private String extraJson;
    private Integer isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
