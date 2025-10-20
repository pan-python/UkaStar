package com.ukastar.infra.catalog;

import com.ukastar.domain.catalog.RewardItem;
import com.ukastar.repo.catalog.RewardItemRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存兑换奖励仓储。
 */
@Repository
public class InMemoryRewardItemRepository implements RewardItemRepository {

    private final Map<Long, RewardItem> rewards = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(2000);

    public InMemoryRewardItemRepository() {
    }

    @Override
    public Flux<RewardItem> findByTenantId(Long tenantId) {
        return Flux.fromStream(rewards.values().stream().filter(item -> item.tenantId().equals(tenantId)));
    }

    @Override
    public Mono<RewardItem> findById(Long id) {
        return Mono.justOrEmpty(rewards.get(id));
    }

    @Override
    public Mono<RewardItem> save(RewardItem item) {
        RewardItem persisted = item;
        if (item.id() == null) {
            long id = sequence.incrementAndGet();
            persisted = new RewardItem(id, item.tenantId(), item.name(), item.cost(), item.systemDefault(), Instant.now(), Instant.now());
        }
        rewards.put(persisted.id(), persisted);
        return Mono.just(persisted);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        rewards.remove(id);
        return Mono.empty();
    }
}
