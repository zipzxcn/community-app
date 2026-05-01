package com.community.backend.dto.admin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminPostQueryRequest {

    @Min(value = 1, message = "页码必须大于等于1")
    private Long page = 1L;

    @Min(value = 1, message = "分页大小必须大于等于1")
    private Long size = 10L;

    @Pattern(regexp = "ALL|PUBLISHED|HIDDEN", message = "状态仅支持 ALL、PUBLISHED 或 HIDDEN")
    private String status = "ALL";

    @Size(max = 64, message = "关键词长度不能超过64位")
    private String keyword;
}
