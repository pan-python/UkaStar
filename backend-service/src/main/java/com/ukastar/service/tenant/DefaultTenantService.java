package com.ukastar.service.tenant;

import com.ukastar.domain.tenant.Tenant;
import com.ukastar.domain.tenant.TenantStatus;
import com.ukastar.repo.tenant.TenantRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * 默认租户服务实现。
 */
@Service
public class DefaultTenantService implements TenantService {

    private final TenantRepository tenantRepository;

    public DefaultTenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public Flux<Tenant> listTenants() {
        return tenantRepository.findAll();
    }

    @Override
    public Mono<Tenant> createTenant(String code, String name, String contactName, String contactPhone) {
        Tenant tenant = new Tenant(null, code, name, contactName, contactPhone, TenantStatus.ACTIVE, Instant.now(), Instant.now());
        return tenantRepository.save(tenant);
    }

    @Override
    public Mono<Tenant> updateTenant(Long id, String name, String contactName, String contactPhone) {
        return tenantRepository.findById(id)
                .map(existing -> existing.updateProfile(name, contactName, contactPhone))
                .flatMap(tenantRepository::save);
    }

    @Override
    public Mono<Tenant> activate(Long id) {
        return tenantRepository.findById(id)
                .map(Tenant::activate)
                .flatMap(tenantRepository::save);
    }

    @Override
    public Mono<Tenant> suspend(Long id) {
        return tenantRepository.findById(id)
                .map(Tenant::suspend)
                .flatMap(tenantRepository::save);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return tenantRepository.deleteById(id);
    }
}
