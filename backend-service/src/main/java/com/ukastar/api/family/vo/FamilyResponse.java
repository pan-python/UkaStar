package com.ukastar.api.family.vo;

import java.time.Instant;
import java.util.List;

/**
 * 家庭响应。
 */
public record FamilyResponse(
        Long id,
        Long tenantId,
        String familyName,
        List<ParentResponse> parents,
        List<ChildResponse> children,
        Instant createdAt,
        Instant updatedAt
) {
}
