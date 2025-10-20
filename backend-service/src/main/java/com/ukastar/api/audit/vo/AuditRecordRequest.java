package com.ukastar.api.audit.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 审计记录请求。
 */
public record AuditRecordRequest(
        @NotNull Long tenantId,
        @NotBlank String eventType,
        @NotBlank String actor,
        @NotBlank String target,
        @NotBlank String summary
) {
}
