package com.ukastar.repo.points;

import com.ukastar.domain.points.PointBalance;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 积分余额仓储。
 */
public interface PointBalanceRepository {

    Mono<PointBalance> findByFamilyId(Long familyId);

    Mono<PointBalance> save(PointBalance balance);

    Flux<PointBalance> findByTenantId(Long tenantId);
}
