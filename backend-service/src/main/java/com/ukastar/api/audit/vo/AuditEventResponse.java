package com.ukastar.api.audit.vo;

import java.time.Instant;

/**
 * 审计事件响应。
 */
public record AuditEventResponse(
        Long id,
        Long tenantId,
        String eventType,
        String actor,
        String target,
        String summary,
        Instant occurredAt
) {
}
