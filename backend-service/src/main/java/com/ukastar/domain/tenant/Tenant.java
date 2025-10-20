package com.ukastar.domain.tenant;

import java.time.Instant;

/**
 * 租户领域模型，覆盖基础状态与联系方式。
 */
public record Tenant(
        Long id,
        String code,
        String name,
        String contactName,
        String contactPhone,
        TenantStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public Tenant activate() {
        return new Tenant(id, code, name, contactName, contactPhone, TenantStatus.ACTIVE, createdAt, Instant.now());
    }

    public Tenant suspend() {
        return new Tenant(id, code, name, contactName, contactPhone, TenantStatus.SUSPENDED, createdAt, Instant.now());
    }

    public Tenant updateProfile(String newName, String newContactName, String newContactPhone) {
        return new Tenant(id, code, newName, newContactName, newContactPhone, status, createdAt, Instant.now());
    }
}
