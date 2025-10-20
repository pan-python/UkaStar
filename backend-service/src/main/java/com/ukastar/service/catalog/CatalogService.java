package com.ukastar.service.catalog;

import com.ukastar.domain.catalog.PointCategory;
import com.ukastar.domain.catalog.PointItem;
import com.ukastar.domain.catalog.RewardItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 积分类别/项目/兑换项服务。
 */
public interface CatalogService {

    Flux<PointCategory> listCategories(Long tenantId);

    Mono<PointCategory> createCategory(Long tenantId, String name, boolean systemDefault);

    Mono<PointCategory> renameCategory(Long categoryId, String name);

    Flux<PointItem> listItems(Long tenantId);

    Mono<PointItem> createItem(Long tenantId, Long categoryId, String name, int score, boolean positive, boolean systemDefault);

    Mono<PointItem> updateItem(Long itemId, String name, int score, boolean positive);

    Mono<Void> deleteItem(Long itemId);

    Flux<RewardItem> listRewards(Long tenantId);

    Mono<RewardItem> createReward(Long tenantId, String name, int cost, boolean systemDefault);

    Mono<RewardItem> updateReward(Long rewardId, String name, int cost);

    Mono<Void> deleteReward(Long rewardId);
}
