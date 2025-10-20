package com.ukastar.infra.family;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.domain.family.Child;
import com.ukastar.domain.family.Family;
import com.ukastar.domain.family.Parent;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.domain.family.Child;
import com.ukastar.domain.family.Parent;
import com.ukastar.persistence.entity.FamilyEntity;
import com.ukastar.persistence.entity.FamilyMemberEntity;
import com.ukastar.persistence.entity.ParentEntity;
import com.ukastar.persistence.entity.ChildEntity;
import com.ukastar.persistence.mapper.FamilyMapper;
import com.ukastar.persistence.mapper.FamilyMemberMapper;
import com.ukastar.persistence.mapper.ParentMapper;
import com.ukastar.persistence.mapper.ChildMapper;
import com.ukastar.repo.family.FamilyRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Repository
@ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "true")
public class MpFamilyRepository implements FamilyRepository {

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final ParentMapper parentMapper;
    private final ChildMapper childMapper;

    public MpFamilyRepository(FamilyMapper familyMapper,
                              FamilyMemberMapper familyMemberMapper,
                              ParentMapper parentMapper,
                              ChildMapper childMapper) {
        this.familyMapper = familyMapper;
        this.familyMemberMapper = familyMemberMapper;
        this.parentMapper = parentMapper;
        this.childMapper = childMapper;
    }

    @Override
    public Flux<Family> findByTenantId(Long tenantId) {
        return Flux.defer(() -> Flux.fromIterable(
                familyMapper.selectList(new QueryWrapper<FamilyEntity>().eq("tenant_id", tenantId))
        )).flatMap(entity -> Mono.fromCallable(() -> toDomainWithMembers(entity)));
    }

    @Override
    public Mono<Family> findById(Long familyId) {
        return Mono.fromCallable(() -> familyMapper.selectById(familyId))
                .flatMap(entity -> entity == null ? Mono.empty() : Mono.fromCallable(() -> toDomainWithMembers(entity)));
    }

    @Override
    public Mono<Family> save(Family family) {
        return Mono.fromCallable(() -> {
            FamilyEntity entity = new FamilyEntity();
            entity.setId(family.id());
            entity.setTenantId(family.tenantId());
            entity.setName(family.familyName());
            if (entity.getId() == null && (entity.getFamilyCode() == null || entity.getFamilyCode().isBlank())) {
                entity.setFamilyCode("F" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8));
            }
            if (entity.getId() == null) {
                familyMapper.insert(entity);
            } else {
                familyMapper.updateById(entity);
            }
            Long id = entity.getId();
            // 同步成员关系：先清理，再插入
            familyMemberMapper.delete(new QueryWrapper<FamilyMemberEntity>().eq("tenant_id", family.tenantId()).eq("family_id", id));
            if (family.parents() != null) {
                for (Parent p : family.parents()) {
                    if (p.id() != null) insertMember(family.tenantId(), id, "PARENT", p.id(), null, 1);
                }
            }
            if (family.children() != null) {
                for (Child c : family.children()) {
                    if (c.id() != null) insertMember(family.tenantId(), id, "CHILD", c.id(), null, 0);
                }
            }
            return id;
        }).flatMap(this::findById);
    }

    private Family toDomainWithMembers(FamilyEntity entity) {
        List<FamilyMemberEntity> members = familyMemberMapper.selectList(new QueryWrapper<FamilyMemberEntity>()
                .eq("tenant_id", entity.getTenantId())
                .eq("family_id", entity.getId()));
        List<Long> parentIds = members.stream().filter(m -> "PARENT".equals(m.getMemberType())).map(FamilyMemberEntity::getMemberId).toList();
        List<Long> childIds = members.stream().filter(m -> "CHILD".equals(m.getMemberType())).map(FamilyMemberEntity::getMemberId).toList();
        List<Parent> parents = parentIds.isEmpty() ? List.of() : parentMapper.selectBatchIds(parentIds).stream()
                .map(this::toParent).toList();
        List<Child> children = childIds.isEmpty() ? List.of() : childMapper.selectBatchIds(childIds).stream()
                .map(this::toChild).toList();
        return new Family(entity.getId(), entity.getTenantId(), entity.getName(), parents, children, Instant.EPOCH, Instant.EPOCH);
    }

    private Parent toParent(ParentEntity e) {
        return new Parent(e.getId(), e.getTenantId(), e.getDisplayName(), e.getPhone(), Instant.EPOCH);
    }

    private Child toChild(ChildEntity e) {
        Instant birthday = e.getBirthday() != null ? e.getBirthday().atStartOfDay(java.time.ZoneOffset.UTC).toInstant() : null;
        return new Child(e.getId(), e.getTenantId(), e.getName(), birthday, Instant.EPOCH);
    }

    private void insertMember(Long tenantId, Long familyId, String type, Long memberId, String relation, int isGuardian) {
        FamilyMemberEntity fm = new FamilyMemberEntity();
        fm.setTenantId(tenantId);
        fm.setFamilyId(familyId);
        fm.setMemberType(type);
        fm.setMemberId(memberId);
        fm.setRelation(relation);
        fm.setIsGuardian(isGuardian);
        familyMemberMapper.insert(fm);
    }
}
