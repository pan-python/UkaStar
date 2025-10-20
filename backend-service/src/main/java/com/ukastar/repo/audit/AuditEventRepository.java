package com.ukastar.repo.audit;

import com.ukastar.domain.audit.AuditEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 审计事件仓储。
 */
public interface AuditEventRepository {

    Mono<AuditEvent> save(AuditEvent event);

    Flux<AuditEvent> findByTenantId(Long tenantId);

    Flux<AuditEvent> findByEventType(String eventType);
}
