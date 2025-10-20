package com.ukastar.domain.catalog;

import java.time.Instant;

/**
 * 积分类别（默认 + 自定义）。
 */
public record PointCategory(
        Long id,
        Long tenantId,
        String name,
        boolean systemDefault,
        Instant createdAt,
        Instant updatedAt
) {
    public PointCategory updateName(String newName) {
        return new PointCategory(id, tenantId, newName, systemDefault, createdAt, Instant.now());
    }
}
