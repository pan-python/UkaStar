package com.ukastar.repo.family.child;

import com.ukastar.domain.family.Child;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 孩子仓储。
 */
public interface ChildRepository {

    Mono<Child> save(Child child);

    Mono<Child> findById(Long id);

    Flux<Child> findByIds(Iterable<Long> ids);
}
