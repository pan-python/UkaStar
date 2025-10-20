package com.ukastar.api.catalog.vo;

import java.time.Instant;

/**
 * 奖励响应。
 */
public record RewardResponse(
        Long id,
        Long tenantId,
        String name,
        int cost,
        boolean systemDefault,
        Instant createdAt,
        Instant updatedAt
) {
}
