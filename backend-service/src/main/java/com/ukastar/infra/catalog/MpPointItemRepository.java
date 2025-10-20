package com.ukastar.infra.catalog;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.domain.catalog.PointItem;
import com.ukastar.persistence.entity.PointItemEntity;
import com.ukastar.persistence.mapper.PointItemMapper;
import com.ukastar.repo.catalog.PointItemRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
@ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "true")
public class MpPointItemRepository implements PointItemRepository {

    private final PointItemMapper mapper;

    public MpPointItemRepository(PointItemMapper mapper) { this.mapper = mapper; }

    @Override
    public Flux<PointItem> findByTenantId(Long tenantId) {
        return Flux.defer(() -> Flux.fromIterable(mapper.selectList(new QueryWrapper<PointItemEntity>().eq("tenant_id", tenantId))))
                .map(this::toDomain);
    }

    @Override
    public Mono<PointItem> findById(Long id) {
        return Mono.fromCallable(() -> mapper.selectById(id)).flatMap(e -> e == null ? Mono.empty() : Mono.just(toDomain(e)));
    }

    @Override
    public Mono<PointItem> save(PointItem item) {
        return Mono.fromCallable(() -> {
            PointItemEntity e = new PointItemEntity();
            e.setId(item.id());
            e.setTenantId(item.tenantId());
            e.setCategoryId(item.categoryId());
            e.setName(item.name());
            if (e.getId() == null && (e.getCode() == null || e.getCode().isBlank())) {
                e.setCode("ITEM-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0,8));
            }
            e.setPoints(item.score());
            e.setAllowNegative(item.positive() ? 0 : 1);
            if (e.getId() == null) mapper.insert(e); else mapper.updateById(e);
            return e.getId();
        }).flatMap(this::findById);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return Mono.fromRunnable(() -> mapper.deleteById(id)).then();
    }

    private PointItem toDomain(PointItemEntity e) {
        boolean positive = e.getAllowNegative() == null ? e.getPoints() != null && e.getPoints() >= 0 : e.getAllowNegative() == 0;
        int score = e.getPoints() == null ? 0 : e.getPoints();
        return new PointItem(e.getId(), e.getTenantId(), e.getCategoryId(), e.getName(), score, positive, false, Instant.EPOCH, Instant.EPOCH);
    }
}
