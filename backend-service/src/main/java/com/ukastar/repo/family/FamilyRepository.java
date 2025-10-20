package com.ukastar.repo.family;

import com.ukastar.domain.family.Family;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 家庭仓储。
 */
public interface FamilyRepository {

    Flux<Family> findByTenantId(Long tenantId);

    Mono<Family> findById(Long familyId);

    Mono<Family> save(Family family);
}
