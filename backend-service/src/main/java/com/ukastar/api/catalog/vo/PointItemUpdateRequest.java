package com.ukastar.api.catalog.vo;

import jakarta.validation.constraints.NotBlank;

/**
 * 积分项目更新请求。
 */
public record PointItemUpdateRequest(
        @NotBlank String name,
        int score,
        boolean positive
) {
}
