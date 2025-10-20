package com.ukastar.domain.catalog;

import java.time.Instant;

/**
 * 积分项目。
 */
public record PointItem(
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
    public PointItem update(String newName, int newScore, boolean newPositive) {
        return new PointItem(id, tenantId, categoryId, newName, newScore, newPositive, systemDefault, createdAt, Instant.now());
    }
}
