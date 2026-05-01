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
@TableName("file_object")
/**
 * 文件对象实体：记录上传文件元数据与业务绑定信息。
 */
public class FileObject {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 上传文件的用户 ID。
     */
    private Long uploaderId;
    /**
     * 对象存储提供方，当前项目主要使用 MinIO。
     */
    private String storageProvider;
    /**
     * 对象存储桶名称。
     */
    private String bucketName;
    /**
     * 对象存储中的对象键，相当于文件在桶内的唯一路径。
     */
    private String objectKey;
    /**
     * 文件访问地址，前端通常直接持有该地址用于展示。
     */
    private String accessUrl;
    /**
     * 原始文件名，便于前端展示给用户。
     */
    private String originalName;
    /**
     * 文件扩展名。
     */
    private String ext;
    /**
     * 文件 MIME 类型，用于浏览器展示或下载。
     */
    private String mimeType;
    /**
     * 文件大小，单位字节。
     */
    private Long sizeBytes;
    /**
     * 图片宽度，非图片文件通常为空。
     */
    private Integer width;
    /**
     * 图片高度，非图片文件通常为空。
     */
    private Integer height;
    /**
     * 文件内容 MD5 校验值，用于去重或完整性校验。
     */
    private String checksumMd5;
    /**
     * 业务场景类型，例如 avatar、post-cover、chat-image。
     */
    private String bizType;
    /**
     * 业务场景关联主键，例如帖子 ID、用户 ID。
     */
    private Long bizId;
    /**
     * 同一业务对象下附件排序值。
     */
    private Integer sortOrder;
    /**
     * 业务状态字段，不同对象有各自的枚举语义，例如 ACTIVE、PUBLISHED、HIDDEN。
     */
    private String status;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 最后更新时间。
     */
    private LocalDateTime updatedAt;
}
