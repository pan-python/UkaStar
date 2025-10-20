package com.ukastar.repo.system;

import com.ukastar.domain.system.SystemInfo;
import reactor.core.publisher.Mono;

/**
 * 提供系统信息的仓储接口，后续可接入数据库或配置中心。
 */
public interface SystemInfoRepository {

    Mono<SystemInfo> findCurrentInfo();
}
