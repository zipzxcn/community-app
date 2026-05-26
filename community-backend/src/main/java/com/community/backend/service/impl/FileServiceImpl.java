package com.community.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.community.backend.config.StorageProperties;
import com.community.backend.common.error.BizException;
import com.community.backend.common.error.ErrorCode;
import com.community.backend.dto.file.CompleteUploadRequest;
import com.community.backend.dto.file.UploadTokenRequest;
import com.community.backend.entity.FileObject;
import com.community.backend.mapper.FileObjectMapper;
import com.community.backend.service.FileService;
import com.community.backend.vo.file.FileDownloadVo;
import com.community.backend.vo.file.FileObjectVo;
import com.community.backend.vo.file.UploadTokenVo;
import io.minio.GetObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 文件服务实现：上传凭证生成、文件元数据回写与删除。
 */
/**
 * 文件服务实现：
 * - 采用“后端签发上传凭证 + 前端直传对象存储 + 后端回写元数据”的三段式上传流程。
 * - 这样可以减少后端带宽压力，并保留业务层对文件归属和访问的控制。
 */
@Service
public class FileServiceImpl implements FileService {

    private final FileObjectMapper fileObjectMapper;
    private final StorageProperties storageProperties;
    private final ObjectProvider<MinioClient> minioClientProvider;

    public FileServiceImpl(FileObjectMapper fileObjectMapper,
                           StorageProperties storageProperties,
                           ObjectProvider<MinioClient> minioClientProvider) {
        this.fileObjectMapper = fileObjectMapper;
        this.storageProperties = storageProperties;
        this.minioClientProvider = minioClientProvider;
    }

    /**
     * 生成上传凭证（当前仅支持 MinIO 预签名 URL）。
     */
    @Override
    public UploadTokenVo createUploadToken(Long currentUserId, UploadTokenRequest request) {
        // 当前阶段仅实现 MinIO 直传，其他 provider 后续扩展
        ensureMinioProvider(); // 确保使用 MinIO 客户端
        MinioClient minioClient = requiredMinioClient(); // 获取 MinIO 客户端
        String bucket = requiredBucket(); // 获取存储桶名称
        // 兜底：首次联调时若 bucket 尚未创建，这里自动创建避免接口报错
        ensureBucketExists(minioClient, bucket);
        // 生成对象key，格式为 user/{userId}/{bizType}/{fileName}，确保唯一性
        String objectKey = buildObjectKey(currentUserId, request.getBizType(), request.getFileName());
        // 生成预签名上传地址，有效期 900 秒
        try {
            String uploadUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(requiredExpireSeconds())
                            .build()
            );
            UploadTokenVo vo = new UploadTokenVo();
            vo.setProvider(storageProperties.getProvider().toUpperCase(Locale.ROOT));
            vo.setUploadUrl(uploadUrl);
            vo.setBucketName(bucket);
            vo.setObjectKey(objectKey);
            vo.setHeaders(Map.of("Content-Type", request.getContentType()));
            return vo;
        } catch (Exception ex) {
            throw BizException.of(ErrorCode.FILE_UPLOAD_TOKEN_FAILED,
                    ErrorCode.FILE_UPLOAD_TOKEN_FAILED.getDefaultMessage() + ": " + ex.getMessage(), ex);
        }
    }

    /**
     * 上传完成回写：
     * - 校验 bucket 与 objectKey 安全边界
     * - 新文件写入，已存在文件更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileObjectVo completeUpload(Long currentUserId, CompleteUploadRequest request) {
        String bucket = requiredBucket();
        if (!bucket.equals(request.getBucketName())) {
            // 校验 bucket 是否匹配
            throw BizException.of(ErrorCode.FILE_BUCKET_MISMATCH);
        }
        String expectedPrefix = "user/" + currentUserId + "/";
        if (!request.getObjectKey().startsWith(expectedPrefix)) {
            // 校验 objectKey 是否以预期前缀开头
            throw BizException.of(ErrorCode.FILE_OBJECT_KEY_INVALID);
        }
        // 服务端兜底校验：确认对象确实已上传且大小与上报一致
        ensureObjectUploaded(request.getBucketName(), request.getObjectKey(), request.getSizeBytes());

        // 校验 objectKey 是否已存在
        FileObject existing = fileObjectMapper.selectOne(new LambdaQueryWrapper<FileObject>()
                .eq(FileObject::getBucketName, request.getBucketName())
                .eq(FileObject::getObjectKey, request.getObjectKey())
                .last("LIMIT 1"));

        String status = request.getBizId() == null ? "UPLOADED" : "BOUND"; // 校验业务场景类型是否为空
        if (existing == null) {
            // 新文件写入
            FileObject created = FileObject.builder()
                    .uploaderId(currentUserId)
                    .storageProvider(storageProperties.getProvider().toUpperCase(Locale.ROOT))
                    .bucketName(request.getBucketName())
                    .objectKey(request.getObjectKey())
                    .accessUrl("")
                    .originalName(request.getOriginalName())
                    .ext(extractExt(request.getOriginalName()))
                    .mimeType(request.getMimeType())
                    .sizeBytes(request.getSizeBytes())
                    .bizType(request.getBizType())
                    .bizId(request.getBizId())
                    .sortOrder(0)
                    .status(status)
                    .build();
            fileObjectMapper.insert(created);
            String stableAccessUrl = buildPublicAccessUrl(created.getId());
            fileObjectMapper.update(null, new LambdaUpdateWrapper<FileObject>()
                    .eq(FileObject::getId, created.getId())
                    .set(FileObject::getAccessUrl, stableAccessUrl));
            created.setAccessUrl(stableAccessUrl);
            return toVo(created);
        }

        if (!currentUserId.equals(existing.getUploaderId())) {
            // 校验文件是否属于当前用户
            throw BizException.of(ErrorCode.FILE_NOT_OWNER);
        }
        String stableAccessUrl = buildPublicAccessUrl(existing.getId()); // 构建公开访问 URL
        // 更新文件元数据
        fileObjectMapper.update(null, new LambdaUpdateWrapper<FileObject>()
                .eq(FileObject::getId, existing.getId())
                .set(FileObject::getAccessUrl, stableAccessUrl)
                .set(FileObject::getOriginalName, request.getOriginalName())
                .set(FileObject::getExt, extractExt(request.getOriginalName()))
                .set(FileObject::getMimeType, request.getMimeType())
                .set(FileObject::getSizeBytes, request.getSizeBytes())
                .set(FileObject::getBizType, request.getBizType())
                .set(FileObject::getBizId, request.getBizId())
                .set(FileObject::getStatus, status));
        FileObject refreshed = fileObjectMapper.selectById(existing.getId()); // 刷新文件对象
        return toVo(refreshed);
    }

    /**
     * 公开文件代理读取：返回后端稳定地址背后的真实文件流。
     */
    @Override
    public FileDownloadVo loadPublicFile(Long fileId) {
        FileObject file = fileObjectMapper.selectById(fileId);
        if (file == null || "DELETED".equalsIgnoreCase(file.getStatus())) {
            throw BizException.of(ErrorCode.FILE_NOT_FOUND_OR_FORBIDDEN);
        }
        if ("minio".equalsIgnoreCase(storageProperties.getProvider())) {
            try {
                MinioClient minioClient = requiredMinioClient();
                return new FileDownloadVo(
                        file.getMimeType(),
                        file.getSizeBytes(),
                        file.getOriginalName(),
                        minioClient.getObject(GetObjectArgs.builder()
                                .bucket(file.getBucketName())
                                .object(file.getObjectKey())
                                .build())
                );
            } catch (Exception ex) {
                throw BizException.of(
                        ErrorCode.FILE_OBJECT_NOT_FOUND,
                        ErrorCode.FILE_OBJECT_NOT_FOUND.getDefaultMessage() + ": " + ex.getMessage(),
                        ex
                );
            }
        }
        throw BizException.of(ErrorCode.STORAGE_PROVIDER_UNSUPPORTED);
    }

    /**
     * 校验对象存储文件已存在，并且大小与回调参数一致。
     */
    private void ensureObjectUploaded(String bucketName, String objectKey, Long expectedSize) {
        if (!"minio".equalsIgnoreCase(storageProperties.getProvider())) {
            return;
        }
        MinioClient minioClient = requiredMinioClient();
        try {
            long actualSize = minioClient.statObject(StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build())
                    .size();
            if (expectedSize != null && actualSize != expectedSize) {
                throw BizException.of(ErrorCode.FILE_SIZE_MISMATCH);
            }
        } catch (BizException ex) {
            throw ex;
        } catch (ErrorResponseException ex) {
            throw BizException.of(
                    ErrorCode.FILE_OBJECT_NOT_FOUND,
                    ErrorCode.FILE_OBJECT_NOT_FOUND.getDefaultMessage() + ": " + ex.errorResponse().code(),
                    ex
            );
        } catch (Exception ex) {
            throw BizException.of(
                    ErrorCode.FILE_OBJECT_NOT_FOUND,
                    ErrorCode.FILE_OBJECT_NOT_FOUND.getDefaultMessage() + ": " + ex.getMessage(),
                    ex
            );
        }
    }

    /**
     * 删除文件：
     * - 先逻辑删除数据库状态
     * - 再尝试物理删除对象存储文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long currentUserId, Long fileId) {
        FileObject file = fileObjectMapper.selectById(fileId);
        if (file == null || !currentUserId.equals(file.getUploaderId())) {
            throw BizException.of(ErrorCode.FILE_NOT_FOUND_OR_FORBIDDEN);
        }

        // 先逻辑删除元数据，避免物理删除失败导致数据库状态混乱
        fileObjectMapper.update(null, new LambdaUpdateWrapper<FileObject>()
                .eq(FileObject::getId, fileId)
                .set(FileObject::getStatus, "DELETED"));

        if ("minio".equalsIgnoreCase(storageProperties.getProvider())) {
            try {
                MinioClient minioClient = minioClientProvider.getIfAvailable();
                if (minioClient != null) {
                    minioClient.removeObject(RemoveObjectArgs.builder()
                            .bucket(file.getBucketName())
                            .object(file.getObjectKey())
                            .build());
                }
            } catch (Exception ex) {
                // 物理删除失败不回滚业务删除状态，避免接口重试时反复失败
            }
        }
    }

    /**
     * 校验当前 provider 是否为 minio。
     */
    private void ensureMinioProvider() {
        if (!"minio".equalsIgnoreCase(storageProperties.getProvider())) {
            throw BizException.of(ErrorCode.STORAGE_PROVIDER_UNSUPPORTED);
        }
    }

    /**
     * 获取 MinIO 客户端实例，未初始化则抛错。
     */
    private MinioClient requiredMinioClient() {
        MinioClient minioClient = minioClientProvider.getIfAvailable();
        if (minioClient == null) {
            throw BizException.of(ErrorCode.STORAGE_MINIO_CLIENT_NOT_READY);
        }
        return minioClient;
    }

    /**
     * 确保 bucket 存在，不存在则自动创建。
     */
    private void ensureBucketExists(MinioClient minioClient, String bucket) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception ex) {
            throw BizException.of(
                    ErrorCode.FILE_UPLOAD_TOKEN_FAILED,
                    ErrorCode.FILE_UPLOAD_TOKEN_FAILED.getDefaultMessage() + ": " + ex.getMessage(),
                    ex
            );
        }
    }

    /**
     * 获取 bucket 配置。
     */
    private String requiredBucket() {
        if (!StringUtils.hasText(storageProperties.getBucket())) {
            throw BizException.of(ErrorCode.STORAGE_BUCKET_NOT_CONFIGURED);
        }
        return storageProperties.getBucket();
    }

    /**
     * 上传凭证过期时间保护：默认 900 秒，最大 7 天。
     */
    private int requiredExpireSeconds() {
        int expire = storageProperties.getUploadExpireSeconds() == null ? 900 : storageProperties.getUploadExpireSeconds();
        if (expire <= 0) {
            return 900;
        }
        return Math.min(expire, 604800);
    }

    /**
     * 生成对象存储 key：按用户/业务类型/日期分层。
     */
    private String buildObjectKey(Long userId, String bizType, String fileName) {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String ext = extractExt(fileName);
        String normalizedBiz = (bizType == null ? "COMMON" : bizType.trim().toUpperCase(Locale.ROOT));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String suffix = StringUtils.hasText(ext) ? "." + ext : "";
        return "user/" + userId + "/" + normalizedBiz + "/" + date + "/" + uuid + suffix;
    }

    /**
     * 统一生成经由后端代理的稳定访问地址，避免浏览器直接访问私有桶对象。
     */
    private String buildPublicAccessUrl(Long fileId) {
        return "/api/v1/files/public/" + fileId;
    }

    /**
     * 提取并规范化文件后缀（最长 16 字符）。
     */
    private String extractExt(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return "";
        }
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).trim().toLowerCase(Locale.ROOT);
        return ext.length() > 16 ? ext.substring(0, 16) : ext;
    }

    /**
     * 文件实体转 VO。
     */
    private FileObjectVo toVo(FileObject file) {
        FileObjectVo vo = new FileObjectVo();
        vo.setId(file.getId());
        vo.setAccessUrl(file.getAccessUrl());
        vo.setOriginalName(file.getOriginalName());
        vo.setMimeType(file.getMimeType());
        vo.setSizeBytes(file.getSizeBytes());
        vo.setBizType(file.getBizType());
        vo.setBizId(file.getBizId());
        return vo;
    }
}
