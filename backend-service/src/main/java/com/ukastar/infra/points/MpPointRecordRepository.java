package com.ukastar.infra.points;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.domain.points.PointActionType;
import com.ukastar.domain.points.PointRecord;
import com.ukastar.persistence.entity.PointRecordEntity;
import com.ukastar.persistence.mapper.PointRecordMapper;
import com.ukastar.repo.points.PointRecordRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Repository
@ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "true")
public class MpPointRecordRepository implements PointRecordRepository {

    private final PointRecordMapper mapper;

    public MpPointRecordRepository(PointRecordMapper mapper) { this.mapper = mapper; }

    @Override
    public Flux<PointRecord> findByTenantId(Long tenantId) {
        return Flux.defer(() -> Flux.fromIterable(mapper.selectList(new QueryWrapper<PointRecordEntity>().eq("tenant_id", tenantId))))
                .map(this::toDomain);
    }

    @Override
    public Flux<PointRecord> findByChildId(Long childId) {
        return Flux.defer(() -> Flux.fromIterable(mapper.selectList(new QueryWrapper<PointRecordEntity>().eq("child_id", childId))))
                .map(this::toDomain);
    }

    @Override
    public Flux<PointRecord> findRecent(int limit) {
        return Flux.defer(() -> Flux.fromIterable(mapper.selectList(new QueryWrapper<PointRecordEntity>().orderByDesc("occurred_at").last("limit " + limit))))
                .map(this::toDomain);
    }

    @Override
    public Flux<PointRecord> saveAll(Iterable<PointRecord> records) {
        List<PointRecord> saved = new ArrayList<>();
        for (PointRecord r : records) {
            PointRecordEntity e = new PointRecordEntity();
            e.setId(r.id());
            e.setTenantId(r.tenantId());
            e.setRecordType(toDbType(r.actionType()));
            e.setChildId(r.childId());
            e.setOperatorAccountId(r.operatorAccountId());
            e.setQuantity(1);
            e.setPoints(r.amount());
            int before = computeBefore(r);
            e.setBeforePoints(before);
            e.setAfterPoints(r.balanceAfter());
            e.setOccurredAt(LocalDateTime.ofInstant(r.occurredAt(), ZoneOffset.UTC));
            e.setRemark(r.reason());
            if (e.getId() == null) mapper.insert(e); else mapper.updateById(e);
            saved.add(toDomain(e));
        }
        return Flux.fromIterable(saved);
    }

    private String toDbType(PointActionType t) {
        return switch (t) {
            case AWARD -> "INCREASE";
            case DEDUCT -> "DECREASE";
            case REDEEM -> "REDEEM";
        };
    }

    private PointActionType toDomainType(String db) {
        return switch (db) {
            case "INCREASE" -> PointActionType.AWARD;
            case "DECREASE" -> PointActionType.DEDUCT;
            default -> PointActionType.REDEEM;
        };
    }

    private int computeBefore(PointRecord r) {
        int signed = switch (r.actionType()) {
            case AWARD -> r.amount();
            case DEDUCT, REDEEM -> -r.amount();
        };
        return r.balanceAfter() - signed;
    }

    private PointRecord toDomain(PointRecordEntity e) {
        Instant ts = e.getOccurredAt() != null ? e.getOccurredAt().toInstant(ZoneOffset.UTC) : Instant.EPOCH;
        return new PointRecord(e.getId(), e.getTenantId(), e.getChildId(), e.getOperatorAccountId(), toDomainType(e.getRecordType()), e.getPoints() == null ? 0 : e.getPoints(), e.getAfterPoints() == null ? 0 : e.getAfterPoints(), e.getRemark(), ts);
    }
}

