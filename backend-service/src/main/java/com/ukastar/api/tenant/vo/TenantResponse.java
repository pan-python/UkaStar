package com.ukastar.api.tenant.vo;

import com.ukastar.domain.tenant.TenantStatus;

import java.time.Instant;

/**
 * 租户响应。
 */
public record TenantResponse(
        Long id,
        String code,
        String name,
        String contactName,
        String contactPhone,
        TenantStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
