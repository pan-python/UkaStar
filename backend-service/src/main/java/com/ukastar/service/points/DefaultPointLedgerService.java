package com.ukastar.service.points;

import com.ukastar.common.error.ErrorCode;
import com.ukastar.common.exception.BusinessException;
import com.ukastar.domain.family.Child;
import com.ukastar.domain.points.PointActionType;
import com.ukastar.domain.points.PointBalance;
import com.ukastar.domain.points.PointRecord;
import com.ukastar.domain.points.PointStatistics;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.persistence.entity.DailyPointsEntity;
import com.ukastar.persistence.mapper.DailyPointsMapper;
import com.ukastar.repo.points.PointBalanceRepository;
import com.ukastar.repo.points.PointRecordRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存积分流水实现。
 */
@Service
public class DefaultPointLedgerService implements PointLedgerService {

    private final PointBalanceRepository pointBalanceRepository;
    private final PointRecordRepository pointRecordRepository;
    private final com.ukastar.repo.family.child.ChildRepository childRepository;
    private final AtomicLong recordSequence = new AtomicLong(1);
    private final org.springframework.beans.factory.ObjectProvider<DailyPointsMapper> dailyPointsMapperProvider;

    public DefaultPointLedgerService(PointBalanceRepository pointBalanceRepository,
                                     PointRecordRepository pointRecordRepository,
                                     com.ukastar.repo.family.child.ChildRepository childRepository,
                                     org.springframework.beans.factory.ObjectProvider<DailyPointsMapper> dailyPointsMapperProvider) {
        this.pointBalanceRepository = pointBalanceRepository;
        this.pointRecordRepository = pointRecordRepository;
        this.childRepository = childRepository;
        this.dailyPointsMapperProvider = dailyPointsMapperProvider;
    }

    @Override
    public Mono<PointBalance> award(Long childId, int amount, Long operatorAccountId, String reason) {
        return applyChange(childId, amount, operatorAccountId, reason, PointActionType.AWARD);
    }

    @Override
    public Mono<PointBalance> deduct(Long childId, int amount, Long operatorAccountId, String reason) {
        return applyChange(childId, -Math.abs(amount), operatorAccountId, reason, PointActionType.DEDUCT);
    }

    @Override
    public Mono<PointBalance> redeem(Long childId, int amount, Long operatorAccountId, String reason) {
        return applyChange(childId, -Math.abs(amount), operatorAccountId, reason, PointActionType.REDEEM);
    }

    @Override
    public Flux<PointRecord> listRecordsByTenant(Long tenantId) {
        return pointRecordRepository.findByTenantId(tenantId);
    }

    @Override
    public Flux<PointRecord> listRecordsByChild(Long childId) {
        return pointRecordRepository.findByChildId(childId);
    }

    @Override
    public Mono<PointStatistics> statistics(Long tenantId) {
        Mono<List<PointRecord>> recordsMono = pointRecordRepository.findByTenantId(tenantId).collectList();
        Mono<List<PointBalance>> balancesMono = pointBalanceRepository.findByTenantId(tenantId).collectList();
        return Mono.zip(recordsMono, balancesMono)
                .map(tuple -> {
                    List<PointRecord> records = tuple.getT1();
                    List<PointBalance> balances = tuple.getT2();
                    LocalDate today = LocalDate.now(ZoneOffset.UTC);
                    LocalDate weekAgo = today.minusDays(6);
                    int todayCount = (int) records.stream()
                            .filter(record -> LocalDate.ofInstant(record.occurredAt(), ZoneOffset.UTC).equals(today))
                            .count();
                    int todayNet = records.stream()
                            .filter(record -> LocalDate.ofInstant(record.occurredAt(), ZoneOffset.UTC).equals(today))
                            .mapToInt(record -> toSignedAmount(record))
                            .sum();
                    int weeklyNet = records.stream()
                            .filter(record -> {
                                LocalDate date = LocalDate.ofInstant(record.occurredAt(), ZoneOffset.UTC);
                                return !date.isBefore(weekAgo);
                            })
                            .mapToInt(this::toSignedAmount)
                            .sum();
                    int totalPoints = balances.stream().mapToInt(PointBalance::balance).sum();
                    return new PointStatistics(todayCount, todayNet, balances.size(), totalPoints, weeklyNet);
                });
    }

    private int toSignedAmount(PointRecord record) {
        return switch (record.actionType()) {
            case AWARD -> record.amount();
            case DEDUCT, REDEEM -> -record.amount();
        };
    }

    private Mono<PointBalance> applyChange(Long childId, int delta, Long operatorAccountId, String reason, PointActionType type) {
        return childRepository.findById(childId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "孩子不存在")))
                .flatMap(child -> pointBalanceRepository.findByChildId(childId)
                        .defaultIfEmpty(new PointBalance(childId, child.tenantId(), 0))
                        .flatMap(balance -> {
                            int newBalance = balance.balance() + delta;
                            if (newBalance < 0) {
                                return Mono.error(new BusinessException(ErrorCode.BUSINESS_ERROR, "积分不足"));
                            }
                            PointBalance updated = balance.withBalance(newBalance);
                            PointRecord record = new PointRecord(recordSequence.getAndIncrement(), child.tenantId(), childId, operatorAccountId, type, Math.abs(delta), newBalance, reason, Instant.now());
                            return pointBalanceRepository.save(updated)
                                    .flatMap(saved -> pointRecordRepository.saveAll(List.of(record)).then(Mono.just(saved)))
                                    .doOnSuccess(b -> upsertDailyPoints(child.tenantId(), childId, type, Math.abs(delta), newBalance));
                        }));
    }

    private void upsertDailyPoints(Long tenantId, Long childId, PointActionType type, int amount, int balanceAfter) {
        DailyPointsMapper mapper = dailyPointsMapperProvider.getIfAvailable();
        if (mapper == null) return;
        java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneOffset.UTC);
        DailyPointsEntity row = mapper.selectOne(new QueryWrapper<DailyPointsEntity>()
                .eq("tenant_id", tenantId)
                .eq("child_id", childId)
                .eq("stat_date", today));
        if (row == null) {
            row = new DailyPointsEntity();
            row.setTenantId(tenantId);
            row.setChildId(childId);
            row.setStatDate(today);
            row.setTotalIncrease(0);
            row.setTotalDecrease(0);
            row.setTotalRedeem(0);
        }
        switch (type) {
            case AWARD -> row.setTotalIncrease((row.getTotalIncrease() == null ? 0 : row.getTotalIncrease()) + amount);
            case DEDUCT -> row.setTotalDecrease((row.getTotalDecrease() == null ? 0 : row.getTotalDecrease()) + amount);
            case REDEEM -> row.setTotalRedeem((row.getTotalRedeem() == null ? 0 : row.getTotalRedeem()) + amount);
        }
        row.setBalance(balanceAfter);
        if (row.getId() == null) mapper.insert(row); else mapper.updateById(row);
    }
}
