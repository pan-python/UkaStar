package com.ukastar.repo.catalog;

import com.ukastar.domain.catalog.PointItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 积分项目仓储。
 */
public interface PointItemRepository {

    Flux<PointItem> findByTenantId(Long tenantId);

    Mono<PointItem> findById(Long id);

    Mono<PointItem> save(PointItem item);

    Mono<Void> deleteById(Long id);
}
