package com.ukastar.repo.catalog;

import com.ukastar.domain.catalog.PointCategory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 积分类别仓储。
 */
public interface PointCategoryRepository {

    Flux<PointCategory> findByTenantId(Long tenantId);

    Mono<PointCategory> findById(Long id);

    Mono<PointCategory> save(PointCategory category);
}
