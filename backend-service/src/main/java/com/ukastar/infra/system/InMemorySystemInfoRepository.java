package com.ukastar.infra.system;

import com.ukastar.domain.system.SystemInfo;
import com.ukastar.repo.system.SystemInfoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * 基于内存的系统信息实现，后续可替换为数据库或配置中心实现。
 */
@Repository
public class InMemorySystemInfoRepository implements SystemInfoRepository {

    private static final SystemInfo DEFAULT_INFO =
            new SystemInfo("backend-service", "0.0.1-SNAPSHOT", Instant.EPOCH);

    @Override
    public Mono<SystemInfo> findCurrentInfo() {
        return Mono.just(DEFAULT_INFO);
    }
}
