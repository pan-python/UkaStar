package com.ukastar.infra.family.child;

import com.ukastar.domain.family.Child;
import com.ukastar.repo.family.child.ChildRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存孩子仓储。
 */
@Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryChildRepository implements ChildRepository {

    private final Map<Long, Child> children = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(800);

    @Override
    public Mono<Child> save(Child child) {
        Child persisted = child;
        if (child.id() == null) {
            long id = sequence.incrementAndGet();
            persisted = new Child(id, child.tenantId(), child.name(), child.birthday(), child.createdAt());
        }
        children.put(persisted.id(), persisted);
        return Mono.just(persisted);
    }

    @Override
    public Mono<Child> findById(Long id) {
        return Mono.justOrEmpty(children.get(id));
    }

    @Override
    public Flux<Child> findByIds(Iterable<Long> ids) {
        return Flux.fromIterable(ids).flatMap(id -> Mono.justOrEmpty(children.get(id)));
    }
}
