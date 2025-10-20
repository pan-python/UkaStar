package com.ukastar.service.system;

import com.ukastar.domain.system.SystemInfo;
import reactor.core.publisher.Mono;

/**
 * 系统信息查询服务。
 */
public interface SystemInfoService {

    Mono<SystemInfo> current();
}
