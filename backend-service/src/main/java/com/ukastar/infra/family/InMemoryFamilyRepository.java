package com.ukastar.infra.family;

import com.ukastar.domain.family.Family;
import com.ukastar.repo.family.FamilyRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存家庭仓储。
 */
@Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryFamilyRepository implements FamilyRepository {

    private final Map<Long, Family> families = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(200);

    public InMemoryFamilyRepository() {
    }

    @Override
    public Flux<Family> findByTenantId(Long tenantId) {
        return Flux.fromStream(families.values().stream().filter(family -> family.tenantId().equals(tenantId)));
    }

    @Override
    public Mono<Family> findById(Long familyId) {
        return Mono.justOrEmpty(families.get(familyId));
    }

    @Override
    public Mono<Family> save(Family family) {
        Family persisted = family;
        if (family.id() == null) {
            long id = sequence.incrementAndGet();
            persisted = new Family(id, family.tenantId(), family.familyName(), family.parents(), family.children(), Instant.now(), Instant.now());
        }
        families.put(persisted.id(), persisted);
        return Mono.just(persisted);
    }
}
