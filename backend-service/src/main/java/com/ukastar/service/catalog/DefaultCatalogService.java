package com.ukastar.service.catalog;

import com.ukastar.domain.catalog.PointCategory;
import com.ukastar.domain.catalog.PointItem;
import com.ukastar.domain.catalog.RewardItem;
import com.ukastar.repo.catalog.PointCategoryRepository;
import com.ukastar.repo.catalog.PointItemRepository;
import com.ukastar.repo.catalog.RewardItemRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * 默认积分目录服务。
 */
@Service
public class DefaultCatalogService implements CatalogService {

    private final PointCategoryRepository categoryRepository;
    private final PointItemRepository itemRepository;
    private final RewardItemRepository rewardItemRepository;

    public DefaultCatalogService(PointCategoryRepository categoryRepository,
                                 PointItemRepository itemRepository,
                                 RewardItemRepository rewardItemRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.rewardItemRepository = rewardItemRepository;
    }

    @Override
    public Flux<PointCategory> listCategories(Long tenantId) {
        return categoryRepository.findByTenantId(tenantId);
    }

    @Override
    public Mono<PointCategory> createCategory(Long tenantId, String name, boolean systemDefault) {
        PointCategory category = new PointCategory(null, tenantId, name, systemDefault, Instant.now(), Instant.now());
        return categoryRepository.save(category);
    }

    @Override
    public Mono<PointCategory> renameCategory(Long categoryId, String name) {
        return categoryRepository.findById(categoryId)
                .map(existing -> existing.updateName(name))
                .flatMap(categoryRepository::save);
    }

    @Override
    public Flux<PointItem> listItems(Long tenantId) {
        return itemRepository.findByTenantId(tenantId);
    }

    @Override
    public Mono<PointItem> createItem(Long tenantId, Long categoryId, String name, int score, boolean positive, boolean systemDefault) {
        PointItem item = new PointItem(null, tenantId, categoryId, name, score, positive, systemDefault, Instant.now(), Instant.now());
        return itemRepository.save(item);
    }

    @Override
    public Mono<PointItem> updateItem(Long itemId, String name, int score, boolean positive) {
        return itemRepository.findById(itemId)
                .map(existing -> existing.update(name, score, positive))
                .flatMap(itemRepository::save);
    }

    @Override
    public Mono<Void> deleteItem(Long itemId) {
        return itemRepository.deleteById(itemId);
    }

    @Override
    public Flux<RewardItem> listRewards(Long tenantId) {
        return rewardItemRepository.findByTenantId(tenantId);
    }

    @Override
    public Mono<RewardItem> createReward(Long tenantId, String name, int cost, boolean systemDefault) {
        RewardItem item = new RewardItem(null, tenantId, name, cost, systemDefault, Instant.now(), Instant.now());
        return rewardItemRepository.save(item);
    }

    @Override
    public Mono<RewardItem> updateReward(Long rewardId, String name, int cost) {
        return rewardItemRepository.findById(rewardId)
                .map(existing -> existing.update(name, cost))
                .flatMap(rewardItemRepository::save);
    }

    @Override
    public Mono<Void> deleteReward(Long rewardId) {
        return rewardItemRepository.deleteById(rewardId);
    }
}
