package com.ukastar.domain.family;

import java.time.Instant;

/**
 * 孩子实体。
 */
public record Child(
        Long id,
        Long tenantId,
        String name,
        Instant birthday,
        Instant createdAt
) {
}
