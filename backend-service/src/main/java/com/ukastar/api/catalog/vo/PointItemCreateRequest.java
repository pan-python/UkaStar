package com.ukastar.api.catalog.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 积分项目创建请求。
 */
public record PointItemCreateRequest(
        @NotNull Long tenantId,
        @NotNull Long categoryId,
        @NotBlank String name,
        int score,
        boolean positive,
        boolean systemDefault
) {
}
