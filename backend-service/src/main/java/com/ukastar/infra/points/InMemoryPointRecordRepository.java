package com.ukastar.infra.points;

import com.ukastar.domain.points.PointRecord;
import com.ukastar.repo.points.PointRecordRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 内存积分流水仓储。
 */
@Repository
public class InMemoryPointRecordRepository implements PointRecordRepository {

    private final List<PointRecord> records = new ArrayList<>();

    @Override
    public Flux<PointRecord> findByTenantId(Long tenantId) {
        return Flux.fromStream(records.stream().filter(record -> record.tenantId().equals(tenantId)));
    }

    @Override
    public Flux<PointRecord> findByFamilyId(Long familyId) {
        return Flux.fromStream(records.stream().filter(record -> record.familyId().equals(familyId)));
    }

    @Override
    public Flux<PointRecord> findRecent(int limit) {
        Stream<PointRecord> stream = records.stream()
                .sorted(Comparator.comparing(PointRecord::occurredAt).reversed())
                .limit(limit);
        return Flux.fromStream(stream);
    }

    @Override
    public Flux<PointRecord> saveAll(Iterable<PointRecord> entries) {
        for (PointRecord record : entries) {
            records.add(record);
        }
        return Flux.fromIterable(entries);
    }
}
