package com.ukastar.infra.catalog;

import com.ukastar.domain.catalog.PointCategory;
import com.ukastar.repo.catalog.PointCategoryRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存类别仓储。
 */
@Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryPointCategoryRepository implements PointCategoryRepository {

    private final Map<Long, PointCategory> categories = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1000);

    public InMemoryPointCategoryRepository() {
        Instant now = Instant.now();
        categories.put(1L, new PointCategory(1L, 1L, "成长树", true, now, now));
        categories.put(2L, new PointCategory(2L, 2L, "演示积分", true, now, now));
    }

    @Override
    public Flux<PointCategory> findByTenantId(Long tenantId) {
        return Flux.fromStream(categories.values().stream().filter(category -> category.tenantId().equals(tenantId)));
    }

    @Override
    public Mono<PointCategory> findById(Long id) {
        return Mono.justOrEmpty(categories.get(id));
    }

    @Override
    public Mono<PointCategory> save(PointCategory category) {
        PointCategory persisted = category;
        if (category.id() == null) {
            long id = sequence.incrementAndGet();
            persisted = new PointCategory(id, category.tenantId(), category.name(), category.systemDefault(), Instant.now(), Instant.now());
        }
        categories.put(persisted.id(), persisted);
        return Mono.just(persisted);
    }
}
