package com.ukastar.infra.family.parent;

import com.ukastar.domain.family.Parent;
import com.ukastar.repo.family.parent.ParentRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存家长仓储。
 */
@Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryParentRepository implements ParentRepository {

    private final Map<Long, Parent> parents = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(500);

    @Override
    public Mono<Parent> save(Parent parent) {
        Parent persisted = parent;
        if (parent.id() == null) {
            long id = sequence.incrementAndGet();
            persisted = new Parent(id, parent.tenantId(), parent.name(), parent.phoneNumber(), parent.createdAt());
        }
        parents.put(persisted.id(), persisted);
        return Mono.just(persisted);
    }

    @Override
    public Mono<Parent> findById(Long id) {
        return Mono.justOrEmpty(parents.get(id));
    }

    @Override
    public Mono<Parent> findByPhone(Long tenantId, String phoneNumber) {
        return Flux.fromIterable(parents.values())
                .filter(parent -> Objects.equals(parent.tenantId(), tenantId))
                .filter(parent -> parent.phoneNumber().equalsIgnoreCase(phoneNumber))
                .next();
    }

    @Override
    public Flux<Parent> findByIds(Iterable<Long> ids) {
        return Flux.fromIterable(ids).flatMap(id -> Mono.justOrEmpty(parents.get(id)));
    }
}
