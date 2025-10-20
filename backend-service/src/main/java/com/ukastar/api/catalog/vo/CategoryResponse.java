package com.ukastar.api.catalog.vo;

import java.time.Instant;

/**
 * 类别响应。
 */
public record CategoryResponse(
        Long id,
        Long tenantId,
        String name,
        boolean systemDefault,
        Instant createdAt,
        Instant updatedAt
) {
}
