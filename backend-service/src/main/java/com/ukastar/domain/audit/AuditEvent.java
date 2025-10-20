package com.ukastar.domain.audit;

import java.time.Instant;

/**
 * 审计事件记录。
 */
public record AuditEvent(
        Long id,
        Long tenantId,
        String eventType,
        String actor,
        String target,
        String summary,
        Instant occurredAt
) {
}
