package com.ukastar.api.export.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 导出请求。
 */
public record ExportRequest(
        @NotNull Long tenantId,
        @NotBlank String exportType
) {
}
