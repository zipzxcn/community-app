package com.community.backend.vo.post;

import com.community.backend.vo.file.FileObjectVo;
import com.community.backend.vo.user.UserSummaryVo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
/**
 * 帖子详情返回体。
 */
public class PostDetailVo {

    private Long id;
    private String title;
    private String contentMd;
    private String contentHtml;
    private String coverUrl;
    private String status;
    private Integer allowComment;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private LocalDateTime publishedAt;
    private UserSummaryVo author;
    private List<TagVo> tags;
    private List<Long> attachmentFileIds;
    private List<FileObjectVo> attachmentFiles;
    private Boolean liked;
    private Boolean favorited;
}
