package com.ukastar.domain.points;

import java.time.Instant;

/**
 * 积分流水。
 */
public record PointRecord(
        Long id,
        Long tenantId,
        Long familyId,
        Long operatorAccountId,
        PointActionType actionType,
        int amount,
        int balanceAfter,
        String reason,
        Instant occurredAt
) {
}
