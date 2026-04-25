package com.community.backend.service;

import com.community.backend.common.api.PageResponse;
import com.community.backend.dto.draft.SaveDraftRequest;
import com.community.backend.vo.draft.DraftDetailVo;
import com.community.backend.vo.draft.DraftItemVo;

public interface DraftService {

    PageResponse<DraftItemVo> list(Long currentUserId, Long page, Long size);

    DraftDetailVo detail(Long currentUserId, Long draftId);

    Long create(Long currentUserId, SaveDraftRequest request);

    DraftDetailVo update(Long currentUserId, Long draftId, SaveDraftRequest request);

    void delete(Long currentUserId, Long draftId);

    Long publish(Long currentUserId, Long draftId);
}
