package com.community.backend.service;

import com.community.backend.common.api.PageResponse;
import com.community.backend.vo.history.HistoryItemVo;

public interface HistoryService {

    PageResponse<HistoryItemVo> list(Long currentUserId, Long page, Long size);

    void record(Long currentUserId, Long postId);

    void delete(Long currentUserId, Long historyId);

    void clear(Long currentUserId);
}
