package com.ukastar.service.family;

import com.ukastar.domain.family.Child;
import com.ukastar.domain.family.Family;
import com.ukastar.domain.family.Parent;
import com.ukastar.repo.family.FamilyRepository;
import com.ukastar.repo.family.child.ChildRepository;
import com.ukastar.repo.family.parent.ParentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

/**
 * 默认家庭服务实现。
 */
@Service
public class DefaultFamilyService implements FamilyService {

    private final FamilyRepository familyRepository;
    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;

    public DefaultFamilyService(FamilyRepository familyRepository,
                                ParentRepository parentRepository,
                                ChildRepository childRepository) {
        this.familyRepository = familyRepository;
        this.parentRepository = parentRepository;
        this.childRepository = childRepository;
    }

    @Override
    public Flux<Family> listFamilies(Long tenantId) {
        return familyRepository.findByTenantId(tenantId);
    }

    @Override
    public Mono<Family> createFamily(Long tenantId, String familyName) {
        Family family = new Family(null, tenantId, familyName, List.of(), List.of(), Instant.now(), Instant.now());
        return familyRepository.save(family);
    }

    @Override
    public Mono<Family> bindParents(Long familyId, List<Parent> parents) {
        return familyRepository.findById(familyId)
                .flatMap(existing -> Flux.fromIterable(parents)
                        .flatMap(parent -> parent.id() == null
                                ? parentRepository.save(new Parent(null, existing.tenantId(), parent.name(), parent.phoneNumber(), parent.createdAt() != null ? parent.createdAt() : Instant.now()))
                                : parentRepository.findById(parent.id()))
                        .collectList()
                        .flatMap(savedParents -> {
                            Family updated = new Family(existing.id(), existing.tenantId(), existing.familyName(), savedParents, existing.children(), existing.createdAt(), Instant.now());
                            return familyRepository.save(updated);
                        }));
    }

    @Override
    public Mono<Family> bindChildren(Long familyId, List<Child> children) {
        return familyRepository.findById(familyId)
                .flatMap(existing -> Flux.fromIterable(children)
                        .flatMap(child -> child.id() == null
                                ? childRepository.save(new Child(null, existing.tenantId(), child.name(), child.birthday(), child.createdAt() != null ? child.createdAt() : Instant.now()))
                                : childRepository.findById(child.id()))
                        .collectList()
                        .flatMap(savedChildren -> {
                            Family updated = new Family(existing.id(), existing.tenantId(), existing.familyName(), existing.parents(), savedChildren, existing.createdAt(), Instant.now());
                            return familyRepository.save(updated);
                        }));
    }

    @Override
    public Mono<Parent> createParent(Long tenantId, String name, String phoneNumber, Instant createdAt) {
        Parent parent = new Parent(null, tenantId, name, phoneNumber, createdAt != null ? createdAt : Instant.now());
        return parentRepository.save(parent);
    }

    @Override
    public Mono<Child> createChild(Long tenantId, String name, Instant birthday, Instant createdAt) {
        Child child = new Child(null, tenantId, name, birthday, createdAt != null ? createdAt : Instant.now());
        return childRepository.save(child);
    }
}
