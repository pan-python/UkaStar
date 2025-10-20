package com.ukastar.api.family.vo;

import java.time.Instant;

/**
 * 家长响应。
 */
public record ParentResponse(
        Long id,
        Long tenantId,
        String name,
        String phoneNumber,
        Instant createdAt
) {
}
