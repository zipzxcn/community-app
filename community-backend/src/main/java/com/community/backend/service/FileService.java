package com.community.backend.service;

import com.community.backend.dto.file.CompleteUploadRequest;
import com.community.backend.dto.file.UploadTokenRequest;
import com.community.backend.vo.file.FileObjectVo;
import com.community.backend.vo.file.UploadTokenVo;

public interface FileService {

    UploadTokenVo createUploadToken(Long currentUserId, UploadTokenRequest request);

    FileObjectVo completeUpload(Long currentUserId, CompleteUploadRequest request);

    void deleteFile(Long currentUserId, Long fileId);
}
