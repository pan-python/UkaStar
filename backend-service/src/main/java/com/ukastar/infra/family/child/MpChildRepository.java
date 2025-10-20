package com.ukastar.infra.family.child;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.domain.family.Child;
import com.ukastar.persistence.entity.ChildEntity;
import com.ukastar.persistence.mapper.ChildMapper;
import com.ukastar.repo.family.child.ChildRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Repository
@ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "true")
public class MpChildRepository implements ChildRepository {

    private final ChildMapper childMapper;

    public MpChildRepository(ChildMapper childMapper) {
        this.childMapper = childMapper;
    }

    @Override
    public Mono<Child> save(Child child) {
        return Mono.fromCallable(() -> {
            ChildEntity entity = new ChildEntity();
            entity.setId(child.id());
            entity.setTenantId(child.tenantId());
            entity.setName(child.name());
            if (child.birthday() != null) {
                entity.setBirthday(child.birthday().atZone(java.time.ZoneOffset.UTC).toLocalDate());
            }
            if (entity.getId() == null) {
                childMapper.insert(entity);
            } else {
                childMapper.updateById(entity);
            }
            return entity.getId();
        }).flatMap(this::findById);
    }

    @Override
    public Mono<Child> findById(Long id) {
        return Mono.fromCallable(() -> childMapper.selectById(id))
                .flatMap(entity -> entity == null ? Mono.empty() : Mono.just(toDomain(entity)));
    }

    @Override
    public Flux<Child> findByIds(Iterable<Long> ids) {
        return Flux.defer(() -> Flux.fromIterable(childMapper.selectBatchIds(ids))).map(this::toDomain);
    }

    private Child toDomain(ChildEntity entity) {
        Instant birthday = entity.getBirthday() != null ? entity.getBirthday().atStartOfDay(java.time.ZoneOffset.UTC).toInstant() : null;
        return new Child(entity.getId(), entity.getTenantId(), entity.getName(), birthday, Instant.EPOCH);
    }
}

