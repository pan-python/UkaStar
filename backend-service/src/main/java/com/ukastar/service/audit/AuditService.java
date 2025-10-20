package com.ukastar.service.audit;

import com.ukastar.domain.audit.AuditEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 审计日志服务。
 */
public interface AuditService {

    Mono<AuditEvent> recordEvent(Long tenantId, String eventType, String actor, String target, String summary);

    Flux<AuditEvent> listByTenant(Long tenantId);

    Flux<AuditEvent> listByEventType(String eventType);
}
