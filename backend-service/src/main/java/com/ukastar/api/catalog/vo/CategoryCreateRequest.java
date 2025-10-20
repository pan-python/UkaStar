package com.ukastar.api.catalog.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 类别创建请求。
 */
public record CategoryCreateRequest(
        @NotNull Long tenantId,
        @NotBlank String name,
        boolean systemDefault
) {
}
