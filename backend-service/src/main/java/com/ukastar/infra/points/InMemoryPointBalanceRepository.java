package com.ukastar.infra.points;

import com.ukastar.domain.points.PointBalance;
import com.ukastar.repo.points.PointBalanceRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存积分余额仓储。
 */
@Repository
public class InMemoryPointBalanceRepository implements PointBalanceRepository {

    private final Map<Long, PointBalance> balances = new ConcurrentHashMap<>();

    @Override
    public Mono<PointBalance> findByChildId(Long childId) {
        return Mono.justOrEmpty(balances.get(childId));
    }

    @Override
    public Mono<PointBalance> save(PointBalance balance) {
        balances.put(balance.childId(), balance);
        return Mono.just(balance);
    }

    @Override
    public Flux<PointBalance> findByTenantId(Long tenantId) {
        return Flux.fromStream(balances.values().stream().filter(balance -> balance.tenantId().equals(tenantId)));
    }
}
