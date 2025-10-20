package com.ukastar.repo.points;

import com.ukastar.domain.points.PointRecord;
import reactor.core.publisher.Flux;

/**
 * 积分流水仓储。
 */
public interface PointRecordRepository {

    Flux<PointRecord> findByTenantId(Long tenantId);

    Flux<PointRecord> findByChildId(Long childId);

    Flux<PointRecord> findRecent(int limit);

    Flux<PointRecord> saveAll(Iterable<PointRecord> records);
}
