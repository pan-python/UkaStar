package com.ukastar.domain.family;

import java.time.Instant;

/**
 * 家长实体，以手机号为唯一标识。
 */
public record Parent(
        Long id,
        Long tenantId,
        String name,
        String phoneNumber,
        Instant createdAt
) {
}
