package com.ukastar.domain.catalog;

import java.time.Instant;

/**
 * 兑换奖励项。
 */
public record RewardItem(
        Long id,
        Long tenantId,
        String name,
        int cost,
        boolean systemDefault,
        Instant createdAt,
        Instant updatedAt
) {
    public RewardItem update(String newName, int newCost) {
        return new RewardItem(id, tenantId, newName, newCost, systemDefault, createdAt, Instant.now());
    }
}
