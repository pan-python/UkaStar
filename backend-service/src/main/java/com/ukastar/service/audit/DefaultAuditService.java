package com.ukastar.service.audit;

import com.ukastar.domain.audit.AuditEvent;
import com.ukastar.repo.audit.AuditEventRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * 默认审计服务实现。
 */
@Service
public class DefaultAuditService implements AuditService {

    private final AuditEventRepository auditEventRepository;

    public DefaultAuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    public Mono<AuditEvent> recordEvent(Long tenantId, String eventType, String actor, String target, String summary) {
        AuditEvent event = new AuditEvent(null, tenantId, eventType, actor, target, summary, Instant.now());
        return auditEventRepository.save(event);
    }

    @Override
    public Flux<AuditEvent> listByTenant(Long tenantId) {
        return auditEventRepository.findByTenantId(tenantId);
    }

    @Override
    public Flux<AuditEvent> listByEventType(String eventType) {
        return auditEventRepository.findByEventType(eventType);
    }
}
