package com.ukastar.infra.catalog;

import com.ukastar.domain.catalog.PointItem;
import com.ukastar.repo.catalog.PointItemRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存积分项目仓储。
 */
@Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryPointItemRepository implements PointItemRepository {

    private final Map<Long, PointItem> items = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1500);

    public InMemoryPointItemRepository() {
    }

    @Override
    public Flux<PointItem> findByTenantId(Long tenantId) {
        return Flux.fromStream(items.values().stream().filter(item -> item.tenantId().equals(tenantId)));
    }

    @Override
    public Mono<PointItem> findById(Long id) {
        return Mono.justOrEmpty(items.get(id));
    }

    @Override
    public Mono<PointItem> save(PointItem item) {
        PointItem persisted = item;
        if (item.id() == null) {
            long id = sequence.incrementAndGet();
            persisted = new PointItem(id, item.tenantId(), item.categoryId(), item.name(), item.score(), item.positive(), item.systemDefault(), Instant.now(), Instant.now());
        }
        items.put(persisted.id(), persisted);
        return Mono.just(persisted);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        items.remove(id);
        return Mono.empty();
    }
}
