package com.ukastar.service.points;

import com.ukastar.common.error.ErrorCode;
import com.ukastar.common.exception.BusinessException;
import com.ukastar.domain.family.Family;
import com.ukastar.domain.points.PointActionType;
import com.ukastar.domain.points.PointBalance;
import com.ukastar.domain.points.PointRecord;
import com.ukastar.domain.points.PointStatistics;
import com.ukastar.repo.family.FamilyRepository;
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
    private final FamilyRepository familyRepository;
    private final AtomicLong recordSequence = new AtomicLong(1);

    public DefaultPointLedgerService(PointBalanceRepository pointBalanceRepository,
                                     PointRecordRepository pointRecordRepository,
                                     FamilyRepository familyRepository) {
        this.pointBalanceRepository = pointBalanceRepository;
        this.pointRecordRepository = pointRecordRepository;
        this.familyRepository = familyRepository;
    }

    @Override
    public Mono<PointBalance> award(Long familyId, int amount, Long operatorAccountId, String reason) {
        return applyChange(familyId, amount, operatorAccountId, reason, PointActionType.AWARD);
    }

    @Override
    public Mono<PointBalance> deduct(Long familyId, int amount, Long operatorAccountId, String reason) {
        return applyChange(familyId, -Math.abs(amount), operatorAccountId, reason, PointActionType.DEDUCT);
    }

    @Override
    public Mono<PointBalance> redeem(Long familyId, int amount, Long operatorAccountId, String reason) {
        return applyChange(familyId, -Math.abs(amount), operatorAccountId, reason, PointActionType.REDEEM);
    }

    @Override
    public Flux<PointRecord> listRecordsByTenant(Long tenantId) {
        return pointRecordRepository.findByTenantId(tenantId);
    }

    @Override
    public Flux<PointRecord> listRecordsByFamily(Long familyId) {
        return pointRecordRepository.findByFamilyId(familyId);
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

    private Mono<PointBalance> applyChange(Long familyId, int delta, Long operatorAccountId, String reason, PointActionType type) {
        return familyRepository.findById(familyId)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "家庭不存在")))
                .flatMap(family -> pointBalanceRepository.findByFamilyId(familyId)
                        .defaultIfEmpty(new PointBalance(familyId, family.tenantId(), 0))
                        .flatMap(balance -> {
                            int newBalance = balance.balance() + delta;
                            if (newBalance < 0) {
                                return Mono.error(new BusinessException(ErrorCode.BUSINESS_ERROR, "积分不足"));
                            }
                            PointBalance updated = balance.withBalance(newBalance);
                            PointRecord record = new PointRecord(recordSequence.getAndIncrement(), family.tenantId(), familyId, operatorAccountId, type, Math.abs(delta), newBalance, reason, Instant.now());
                            return pointBalanceRepository.save(updated)
                                    .flatMap(saved -> pointRecordRepository.saveAll(List.of(record)).then(Mono.just(saved)));
                        }));
    }
}
