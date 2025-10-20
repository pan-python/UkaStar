package com.ukastar.api.family.vo;

import java.time.Instant;

/**
 * 孩子响应。
 */
public record ChildResponse(
        Long id,
        Long tenantId,
        String name,
        Instant birthday,
        Instant createdAt
) {
}
