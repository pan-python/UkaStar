package com.ukastar.infra.family.parent;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.domain.family.Parent;
import com.ukastar.persistence.entity.ParentEntity;
import com.ukastar.persistence.mapper.ParentMapper;
import com.ukastar.repo.family.parent.ParentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
@ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "true")
public class MpParentRepository implements ParentRepository {

    private final ParentMapper parentMapper;

    public MpParentRepository(ParentMapper parentMapper) {
        this.parentMapper = parentMapper;
    }

    @Override
    public Mono<Parent> save(Parent parent) {
        return Mono.fromCallable(() -> {
            ParentEntity entity = new ParentEntity();
            entity.setId(parent.id());
            entity.setTenantId(parent.tenantId());
            entity.setDisplayName(parent.name());
            entity.setPhone(parent.phoneNumber());
            if (entity.getId() == null) {
                parentMapper.insert(entity);
            } else {
                parentMapper.updateById(entity);
            }
            return entity.getId();
        }).flatMap(this::findById);
    }

    @Override
    public Mono<Parent> findById(Long id) {
        return Mono.fromCallable(() -> parentMapper.selectById(id))
                .flatMap(entity -> entity == null ? Mono.empty() : Mono.just(toDomain(entity)));
    }

    @Override
    public Mono<Parent> findByPhone(Long tenantId, String phoneNumber) {
        return Mono.fromCallable(() -> parentMapper.selectOne(new QueryWrapper<ParentEntity>()
                        .eq("tenant_id", tenantId)
                        .eq("phone", phoneNumber)))
                .flatMap(entity -> entity == null ? Mono.empty() : Mono.just(toDomain(entity)));
    }

    @Override
    public Flux<Parent> findByIds(Iterable<Long> ids) {
        return Flux.defer(() -> Flux.fromIterable(parentMapper.selectBatchIds(ids))).map(this::toDomain);
    }

    private Parent toDomain(ParentEntity entity) {
        return new Parent(entity.getId(), entity.getTenantId(), entity.getDisplayName(), entity.getPhone(), Instant.EPOCH);
    }
}

