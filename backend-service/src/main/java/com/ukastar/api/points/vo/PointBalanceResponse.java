package com.ukastar.api.points.vo;

/**
 * 积分余额响应。
 */
public record PointBalanceResponse(
        Long childId,
        Long tenantId,
        int balance
) {
}
