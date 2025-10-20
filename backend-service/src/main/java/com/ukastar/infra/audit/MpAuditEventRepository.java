package com.ukastar.infra.audit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.domain.audit.AuditEvent;
import com.ukastar.persistence.entity.AuditLogEntity;
import com.ukastar.persistence.mapper.AuditLogMapper;
import com.ukastar.repo.audit.AuditEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
@ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "true")
public class MpAuditEventRepository implements AuditEventRepository {

    private final AuditLogMapper mapper;

    public MpAuditEventRepository(AuditLogMapper mapper) { this.mapper = mapper; }

    @Override
    public Mono<AuditEvent> save(AuditEvent event) {
        return Mono.fromCallable(() -> {
            AuditLogEntity e = new AuditLogEntity();
            e.setId(event.id());
            e.setTenantId(event.tenantId());
            e.setEventType(event.eventType());
            // 将 actor 暂存于 user_agent 字段；将 target 暂存于 targetType
            e.setUserAgent(event.actor());
            e.setTargetType(event.target());
            e.setDetail(event.summary());
            if (e.getId() == null) mapper.insert(e); else mapper.updateById(e);
            return e.getId();
        }).flatMap(this::findByIdInternal);
    }

    @Override
    public Flux<AuditEvent> findByTenantId(Long tenantId) {
        return Flux.defer(() -> Flux.fromIterable(mapper.selectList(new QueryWrapper<AuditLogEntity>().eq("tenant_id", tenantId))))
                .map(this::toDomain);
    }

    @Override
    public Flux<AuditEvent> findByEventType(String eventType) {
        return Flux.defer(() -> Flux.fromIterable(mapper.selectList(new QueryWrapper<AuditLogEntity>().eq("event_type", eventType))))
                .map(this::toDomain);
    }

    private Mono<AuditEvent> findByIdInternal(Long id) {
        return Mono.fromCallable(() -> mapper.selectById(id)).map(this::toDomain);
    }

    private AuditEvent toDomain(AuditLogEntity e) {
        return new AuditEvent(e.getId(), e.getTenantId(), e.getEventType(), e.getUserAgent(), e.getTargetType(), e.getDetail(), Instant.EPOCH);
    }
}

