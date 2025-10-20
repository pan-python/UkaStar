package com.ukastar.service.points;

import com.ukastar.domain.points.PointBalance;
import com.ukastar.domain.points.PointRecord;
import com.ukastar.domain.points.PointStatistics;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 积分操作与统计服务。
 */
public interface PointLedgerService {

    Mono<PointBalance> award(Long childId, int amount, Long operatorAccountId, String reason);

    Mono<PointBalance> deduct(Long childId, int amount, Long operatorAccountId, String reason);

    Mono<PointBalance> redeem(Long childId, int amount, Long operatorAccountId, String reason);

    Flux<PointRecord> listRecordsByTenant(Long tenantId);

    Flux<PointRecord> listRecordsByChild(Long childId);

    Mono<PointStatistics> statistics(Long tenantId);
}
