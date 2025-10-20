package com.ukastar.service.tenant;

import com.ukastar.domain.tenant.Tenant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 租户管理服务。
 */
public interface TenantService {

    Flux<Tenant> listTenants();

    Mono<Tenant> createTenant(String code, String name, String contactName, String contactPhone);

    Mono<Tenant> updateTenant(Long id, String name, String contactName, String contactPhone);

    Mono<Tenant> activate(Long id);

    Mono<Tenant> suspend(Long id);

    Mono<Void> delete(Long id);
}
