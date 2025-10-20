package com.ukastar.infra.catalog;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.domain.catalog.PointCategory;
import com.ukastar.persistence.entity.PointCategoryEntity;
import com.ukastar.persistence.mapper.PointCategoryMapper;
import com.ukastar.repo.catalog.PointCategoryRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
@ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "true")
public class MpPointCategoryRepository implements PointCategoryRepository {

    private final PointCategoryMapper mapper;

    public MpPointCategoryRepository(PointCategoryMapper mapper) { this.mapper = mapper; }

    @Override
    public Flux<PointCategory> findByTenantId(Long tenantId) {
        return Flux.defer(() -> Flux.fromIterable(mapper.selectList(new QueryWrapper<PointCategoryEntity>().eq("tenant_id", tenantId))))
                .map(this::toDomain);
    }

    @Override
    public Mono<PointCategory> findById(Long id) {
        return Mono.fromCallable(() -> mapper.selectById(id))
                .flatMap(e -> e == null ? Mono.empty() : Mono.just(toDomain(e)));
    }

    @Override
    public Mono<PointCategory> save(PointCategory category) {
        return Mono.fromCallable(() -> {
            PointCategoryEntity e = new PointCategoryEntity();
            e.setId(category.id());
            e.setTenantId(category.tenantId());
            e.setName(category.name());
            if (e.getId() == null && (e.getCode() == null || e.getCode().isBlank())) {
                e.setCode("CAT-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0,8));
            }
            if (e.getId() == null) mapper.insert(e); else mapper.updateById(e);
            return e.getId();
        }).flatMap(this::findById);
    }

    private PointCategory toDomain(PointCategoryEntity e) {
        return new PointCategory(e.getId(), e.getTenantId(), e.getName(), false, Instant.EPOCH, Instant.EPOCH);
    }
}
