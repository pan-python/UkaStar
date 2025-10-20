package com.ukastar.service.family;

import com.ukastar.domain.family.Child;
import com.ukastar.domain.family.Family;
import com.ukastar.domain.family.Parent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

/**
 * 家庭/家长/孩子管理服务。
 */
public interface FamilyService {

    Flux<Family> listFamilies(Long tenantId);

    Mono<Family> createFamily(Long tenantId, String familyName);

    Mono<Family> bindParents(Long familyId, List<Parent> parents);

    Mono<Family> bindChildren(Long familyId, List<Child> children);

    Mono<Parent> createParent(Long tenantId, String name, String phoneNumber, Instant createdAt);

    Mono<Child> createChild(Long tenantId, String name, Instant birthday, Instant createdAt);
}
