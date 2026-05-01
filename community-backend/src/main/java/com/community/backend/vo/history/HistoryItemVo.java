package com.community.backend.vo.history;

import com.community.backend.vo.user.UserSummaryVo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 浏览历史列表项返回体。
 */
public class HistoryItemVo {

    /**
     * 浏览历史记录 ID。
     */
    private Long id;
    /**
     * 帖子 ID，关联帖子主表。
     */
    private Long postId;
    /**
     * 历史记录对应帖子标题。
     */
    private String postTitle;
    /**
     * 历史记录对应帖子封面图。
     */
    private String postCoverUrl;
    /**
     * 浏览次数统计。
     */
    private Integer viewCount;
    private LocalDateTime lastViewedAt;
    /**
     * 作者概要信息。
     */
    private UserSummaryVo author;
}
