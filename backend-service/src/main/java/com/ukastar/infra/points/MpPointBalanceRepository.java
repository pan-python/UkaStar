package com.ukastar.infra.points;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.domain.points.PointBalance;
import com.ukastar.persistence.entity.PointRecordEntity;
import com.ukastar.persistence.mapper.PointRecordMapper;
import com.ukastar.repo.points.PointBalanceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 通过最新流水的 after_points 计算当前余额，不单独持久化余额。
 */
@Repository
@ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "true")
public class MpPointBalanceRepository implements PointBalanceRepository {

    private final PointRecordMapper recordMapper;

    public MpPointBalanceRepository(PointRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    @Override
    public Mono<PointBalance> findByChildId(Long childId) {
        return Mono.fromCallable(() ->
                recordMapper.selectList(new QueryWrapper<PointRecordEntity>()
                        .eq("child_id", childId)
                        .orderByDesc("occurred_at")
                        .last("limit 1"))
        ).flatMap(list -> {
            if (list == null || list.isEmpty()) return Mono.empty();
            PointRecordEntity last = list.get(0);
            int balance = last.getAfterPoints() == null ? 0 : last.getAfterPoints();
            return Mono.just(new PointBalance(childId, last.getTenantId(), balance));
        });
    }

    @Override
    public Mono<PointBalance> save(PointBalance balance) {
        // 余额不单独持久化，直接返回传入值
        return Mono.just(balance);
    }

    @Override
    public Flux<PointBalance> findByTenantId(Long tenantId) {
        // 简化：按 child_id 分组取最新 after_points（此处可优化为 SQL 聚合）
        return Mono.fromCallable(() -> recordMapper.selectList(new QueryWrapper<PointRecordEntity>().eq("tenant_id", tenantId))).flatMapMany(list -> {
            java.util.Map<Long, PointRecordEntity> latest = new java.util.HashMap<>();
            for (PointRecordEntity e : list) {
                PointRecordEntity prev = latest.get(e.getChildId());
                if (prev == null || e.getOccurredAt().isAfter(prev.getOccurredAt())) {
                    latest.put(e.getChildId(), e);
                }
            }
            return Flux.fromIterable(latest.values()).map(e -> new PointBalance(e.getChildId(), e.getTenantId(), e.getAfterPoints() == null ? 0 : e.getAfterPoints()));
        });
    }
}

