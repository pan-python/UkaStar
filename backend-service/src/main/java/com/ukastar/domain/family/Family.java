package com.ukastar.domain.family;

import java.time.Instant;
import java.util.List;

/**
 * 家庭聚合根，包含家长与孩子信息。
 */
public record Family(
        Long id,
        Long tenantId,
        String familyName,
        List<Parent> parents,
        List<Child> children,
        Instant createdAt,
        Instant updatedAt
) {

    public Family withMembers(List<Parent> updatedParents, List<Child> updatedChildren) {
        return new Family(id, tenantId, familyName, List.copyOf(updatedParents), List.copyOf(updatedChildren), createdAt, Instant.now());
    }
}
