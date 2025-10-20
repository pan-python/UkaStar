package com.ukastar.api.points.vo;

/**
 * 积分余额响应。
 */
public record PointBalanceResponse(
        Long familyId,
        Long tenantId,
        int balance
) {
}
