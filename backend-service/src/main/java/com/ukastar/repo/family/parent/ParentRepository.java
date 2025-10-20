package com.ukastar.repo.family.parent;

import com.ukastar.domain.family.Parent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 家长仓储。
 */
public interface ParentRepository {

    Mono<Parent> save(Parent parent);

    Mono<Parent> findById(Long id);

    Mono<Parent> findByPhone(Long tenantId, String phoneNumber);

    Flux<Parent> findByIds(Iterable<Long> ids);
}
