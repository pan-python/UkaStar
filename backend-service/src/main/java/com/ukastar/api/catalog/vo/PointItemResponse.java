package com.ukastar.api.catalog.vo;

import java.time.Instant;

/**
 * 积分项目响应。
 */
public record PointItemResponse(
        Long id,
        Long tenantId,
        Long categoryId,
        String name,
        int score,
        boolean positive,
        boolean systemDefault,
        Instant createdAt,
        Instant updatedAt
) {
}
