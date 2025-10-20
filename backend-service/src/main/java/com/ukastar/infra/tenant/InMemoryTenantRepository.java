package com.ukastar.infra.tenant;

import com.ukastar.domain.tenant.Tenant;
import com.ukastar.domain.tenant.TenantStatus;
import com.ukastar.repo.tenant.TenantRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存租户仓储，便于在数据库未就绪时验证业务流程。
 */
@Repository
public class InMemoryTenantRepository implements TenantRepository {

    private final Map<Long, Tenant> tenants = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(100);

    public InMemoryTenantRepository() {
        Instant now = Instant.now();
        Tenant platform = new Tenant(1L, "platform", "平台租户", "平台管理员", "13000000000", TenantStatus.ACTIVE, now, now);
        Tenant sample = new Tenant(2L, "demo", "演示租户", "演示运营", "13100000000", TenantStatus.ACTIVE, now, now);
        tenants.put(platform.id(), platform);
        tenants.put(sample.id(), sample);
    }

    @Override
    public Flux<Tenant> findAll() {
        return Flux.fromIterable(tenants.values()).sort((a, b) -> a.id().compareTo(b.id()));
    }

    @Override
    public Mono<Tenant> findById(Long id) {
        return Mono.justOrEmpty(tenants.get(id));
    }

    @Override
    public Mono<Tenant> save(Tenant tenant) {
        Tenant persisted = tenant;
        if (tenant.id() == null) {
            long id = sequence.incrementAndGet();
            persisted = new Tenant(id, tenant.code(), tenant.name(), tenant.contactName(), tenant.contactPhone(), tenant.status(), Instant.now(), Instant.now());
        }
        tenants.put(persisted.id(), persisted);
        return Mono.just(persisted);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        tenants.remove(id);
        return Mono.empty();
    }
}
