package com.ukastar.service.system.impl;

import com.ukastar.domain.system.SystemInfo;
import com.ukastar.repo.system.SystemInfoRepository;
import com.ukastar.security.support.CurrentUserProvider;
import com.ukastar.service.system.SystemInfoService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 系统信息服务默认实现，演示 service 层组合仓储与安全上下文。
 */
@Service
public class DefaultSystemInfoService implements SystemInfoService {

    private final SystemInfoRepository repository;
    private final CurrentUserProvider currentUserProvider;

    public DefaultSystemInfoService(SystemInfoRepository repository, CurrentUserProvider currentUserProvider) {
        this.repository = repository;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public Mono<SystemInfo> current() {
        return currentUserProvider.currentUser()
                .flatMap(user -> repository.findCurrentInfo());
    }
}
