package com.ukastar.infra.audit;

import com.ukastar.domain.audit.AuditEvent;
import com.ukastar.repo.audit.AuditEventRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存审计仓储。
 */
@Repository
public class InMemoryAuditEventRepository implements AuditEventRepository {

    private final List<AuditEvent> events = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Mono<AuditEvent> save(AuditEvent event) {
        AuditEvent persisted = event;
        if (event.id() == null) {
            long id = sequence.getAndIncrement();
            persisted = new AuditEvent(id, event.tenantId(), event.eventType(), event.actor(), event.target(), event.summary(), Instant.now());
        }
        events.add(persisted);
        return Mono.just(persisted);
    }

    @Override
    public Flux<AuditEvent> findByTenantId(Long tenantId) {
        return Flux.fromStream(events.stream().filter(entry -> entry.tenantId().equals(tenantId)));
    }

    @Override
    public Flux<AuditEvent> findByEventType(String eventType) {
        return Flux.fromStream(events.stream().filter(entry -> entry.eventType().equalsIgnoreCase(eventType)));
    }
}
