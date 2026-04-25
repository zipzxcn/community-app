package com.community.backend.controller;

import com.community.backend.common.api.ApiResponse;
import com.community.backend.dto.file.CompleteUploadRequest;
import com.community.backend.dto.file.UploadTokenRequest;
import com.community.backend.security.SecurityUtils;
import com.community.backend.service.FileService;
import com.community.backend.vo.file.FileObjectVo;
import com.community.backend.vo.file.UploadTokenVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件控制器：上传凭证、上传完成回写与删除文件。
 */
@Tag(name = "File", description = "文件上传相关接口")
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 生成对象存储直传凭证（当前实现为 MinIO）。
     */
    @Operation(summary = "获取上传凭证", description = "用于前端直传对象存储")
    @PostMapping("/upload-token")
    public ApiResponse<UploadTokenVo> uploadToken(@Valid @RequestBody UploadTokenRequest request) {
        // 获取当前用户上传凭证
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(fileService.createUploadToken(currentUserId, request));
    }

    /**
     * 上传完成回调：登记文件元数据并返回文件信息。
     */
    @Operation(summary = "上传完成回调", description = "上传成功后登记文件元数据")
    @PostMapping("/complete")
    public ApiResponse<FileObjectVo> complete(@Valid @RequestBody CompleteUploadRequest request) {
        // 写入或更新文件元数据
        Long currentUserId = SecurityUtils.requireUserId();
        return ApiResponse.success(fileService.completeUpload(currentUserId, request));
    }

    /**
     * 删除文件：只允许上传者删除，先逻辑删除再尝试物理删除。
     */
    @Operation(summary = "删除文件", description = "逻辑删除文件并尝试删除对象存储文件")
    @DeleteMapping("/{fileId}")
    public ApiResponse<Void> delete(@PathVariable Long fileId) {
        // 仅允许上传者删除自己的文件
        Long currentUserId = SecurityUtils.requireUserId();
        fileService.deleteFile(currentUserId, fileId);
        return ApiResponse.success(null);
    }
}
