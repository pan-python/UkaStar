package com.ukastar.repo.tenant;

import com.ukastar.domain.tenant.Tenant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 租户仓储抽象。
 */
public interface TenantRepository {

    Flux<Tenant> findAll();

    Mono<Tenant> findById(Long id);

    Mono<Tenant> save(Tenant tenant);

    Mono<Void> deleteById(Long id);
}
