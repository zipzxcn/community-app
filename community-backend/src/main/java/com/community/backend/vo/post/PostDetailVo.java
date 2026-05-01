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

    /**
     * 帖子 ID。
     */
    private Long id;
    /**
     * 标题文本，用于帖子、通知等场景。
     */
    private String title;
    /**
     * Markdown 原始内容，便于再次编辑。
     */
    private String contentMd;
    /**
     * 服务端渲染后的 HTML 内容，便于详情页直接展示。
     */
    private String contentHtml;
    /**
     * 封面图地址。
     */
    private String coverUrl;
    /**
     * 业务状态字段，不同对象有各自的枚举语义，例如 ACTIVE、PUBLISHED、HIDDEN。
     */
    private String status;
    /**
     * 是否允许评论，1/0 或 true/false 取决于所在层的表示方式。
     */
    private Integer allowComment;
    /**
     * 浏览次数统计。
     */
    private Integer viewCount;
    /**
     * 点赞数量统计。
     */
    private Integer likeCount;
    /**
     * 收藏数量统计。
     */
    private Integer favoriteCount;
    /**
     * 评论类未读数量或评论总数。
     */
    private Integer commentCount;
    /**
     * 发布时间，未发布数据通常为空。
     */
    private LocalDateTime publishedAt;
    /**
     * 作者概要信息。
     */
    private UserSummaryVo author;
    /**
     * 标签明细列表，前端展示时直接使用。
     */
    private List<TagVo> tags;
    /**
     * 附件文件 ID 列表，便于前后端只传引用。
     */
    private List<Long> attachmentFileIds;
    /**
     * 附件文件明细列表，前端可直接渲染。
     */
    private List<FileObjectVo> attachmentFiles;
    /**
     * 当前登录用户是否已点赞。
     */
    private Boolean liked;
    /**
     * 当前登录用户是否已收藏。
     */
    private Boolean favorited;
}
