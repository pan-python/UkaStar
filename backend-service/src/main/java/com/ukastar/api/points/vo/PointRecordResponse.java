package com.ukastar.api.points.vo;

import com.ukastar.domain.points.PointActionType;

import java.time.Instant;

/**
 * 积分流水响应。
 */
public record PointRecordResponse(
        Long id,
        Long tenantId,
        Long childId,
        Long operatorAccountId,
        PointActionType actionType,
        int amount,
        int balanceAfter,
        String reason,
        Instant occurredAt
) {
}
