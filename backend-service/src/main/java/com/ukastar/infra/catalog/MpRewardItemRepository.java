package com.ukastar.infra.catalog;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.domain.catalog.RewardItem;
import com.ukastar.persistence.entity.RewardItemEntity;
import com.ukastar.persistence.mapper.RewardItemMapper;
import com.ukastar.repo.catalog.RewardItemRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
@ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "true")
public class MpRewardItemRepository implements RewardItemRepository {

    private final RewardItemMapper mapper;

    public MpRewardItemRepository(RewardItemMapper mapper) { this.mapper = mapper; }

    @Override
    public Flux<RewardItem> findByTenantId(Long tenantId) {
        return Flux.defer(() -> Flux.fromIterable(mapper.selectList(new QueryWrapper<RewardItemEntity>().eq("tenant_id", tenantId))))
                .map(this::toDomain);
    }

    @Override
    public Mono<RewardItem> findById(Long id) {
        return Mono.fromCallable(() -> mapper.selectById(id)).flatMap(e -> e == null ? Mono.empty() : Mono.just(toDomain(e)));
    }

    @Override
    public Mono<RewardItem> save(RewardItem item) {
        return Mono.fromCallable(() -> {
            RewardItemEntity e = new RewardItemEntity();
            e.setId(item.id());
            e.setTenantId(item.tenantId());
            e.setName(item.name());
            if (e.getId() == null && (e.getCode() == null || e.getCode().isBlank())) {
                e.setCode("REWARD-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0,8));
            }
            e.setCost(item.cost());
            if (e.getId() == null) mapper.insert(e); else mapper.updateById(e);
            return e.getId();
        }).flatMap(this::findById);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return Mono.fromRunnable(() -> mapper.deleteById(id)).then();
    }

    private RewardItem toDomain(RewardItemEntity e) {
        int cost = e.getCost() == null ? 0 : e.getCost();
        return new RewardItem(e.getId(), e.getTenantId(), e.getName(), cost, false, Instant.EPOCH, Instant.EPOCH);
    }
}
