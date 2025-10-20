package com.ukastar.api.tenant;

import com.ukastar.api.tenant.vo.TenantCreateRequest;
import com.ukastar.api.tenant.vo.TenantResponse;
import com.ukastar.api.tenant.vo.TenantUpdateRequest;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.service.tenant.TenantService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 租户管理接口。
 */
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_TENANT_MANAGE')")
    public Flux<TenantResponse> listTenants() {
        return tenantService.listTenants()
                .map(tenant -> new TenantResponse(tenant.id(), tenant.code(), tenant.name(), tenant.contactName(), tenant.contactPhone(), tenant.status(), tenant.createdAt(), tenant.updatedAt()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_TENANT_MANAGE')")
    public Mono<ApiResponse<TenantResponse>> create(@RequestBody Mono<@Valid TenantCreateRequest> requestMono) {
        return requestMono.flatMap(request -> tenantService.createTenant(request.code(), request.name(), request.contactName(), request.contactPhone()))
                .map(tenant -> new TenantResponse(tenant.id(), tenant.code(), tenant.name(), tenant.contactName(), tenant.contactPhone(), tenant.status(), tenant.createdAt(), tenant.updatedAt()))
                .map(ApiResponse::success);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_TENANT_MANAGE')")
    public Mono<ApiResponse<TenantResponse>> update(@PathVariable Long id, @RequestBody Mono<@Valid TenantUpdateRequest> requestMono) {
        return requestMono.flatMap(request -> tenantService.updateTenant(id, request.name(), request.contactName(), request.contactPhone()))
                .map(tenant -> new TenantResponse(tenant.id(), tenant.code(), tenant.name(), tenant.contactName(), tenant.contactPhone(), tenant.status(), tenant.createdAt(), tenant.updatedAt()))
                .map(ApiResponse::success);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('PERM_TENANT_MANAGE')")
    public Mono<ApiResponse<TenantResponse>> activate(@PathVariable Long id) {
        return tenantService.activate(id)
                .map(tenant -> new TenantResponse(tenant.id(), tenant.code(), tenant.name(), tenant.contactName(), tenant.contactPhone(), tenant.status(), tenant.createdAt(), tenant.updatedAt()))
                .map(ApiResponse::success);
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('PERM_TENANT_MANAGE')")
    public Mono<ApiResponse<TenantResponse>> suspend(@PathVariable Long id) {
        return tenantService.suspend(id)
                .map(tenant -> new TenantResponse(tenant.id(), tenant.code(), tenant.name(), tenant.contactName(), tenant.contactPhone(), tenant.status(), tenant.createdAt(), tenant.updatedAt()))
                .map(ApiResponse::success);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERM_TENANT_MANAGE')")
    public Mono<ApiResponse<Void>> delete(@PathVariable Long id) {
        return tenantService.delete(id).thenReturn(ApiResponse.success());
    }
}
