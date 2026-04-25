package com.community.backend.vo.history;

import com.community.backend.vo.user.UserSummaryVo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 浏览历史列表项返回体。
 */
public class HistoryItemVo {

    private Long id;
    private Long postId;
    private String postTitle;
    private String postCoverUrl;
    private Integer viewCount;
    private LocalDateTime lastViewedAt;
    private UserSummaryVo author;
}
