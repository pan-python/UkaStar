package com.ukastar.repo.catalog;

import com.ukastar.domain.catalog.RewardItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 兑换奖励仓储。
 */
public interface RewardItemRepository {

    Flux<RewardItem> findByTenantId(Long tenantId);

    Mono<RewardItem> findById(Long id);

    Mono<RewardItem> save(RewardItem item);

    Mono<Void> deleteById(Long id);
}
