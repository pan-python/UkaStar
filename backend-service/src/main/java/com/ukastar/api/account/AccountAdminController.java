package com.ukastar.api.account;

import com.ukastar.api.account.vo.AccountCreateRequest;
import com.ukastar.api.account.vo.AccountResponse;
import com.ukastar.api.account.vo.AccountRoleUpdateRequest;
import com.ukastar.api.account.vo.AccountToggleRequest;
import com.ukastar.api.account.vo.RoleMutationRequest;
import com.ukastar.api.account.vo.RoleResponse;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.domain.rbac.Role;
import com.ukastar.service.account.AccountAdminService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * 账号与角色管理接口。
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountAdminController {

    private final AccountAdminService accountAdminService;

    public AccountAdminController(AccountAdminService accountAdminService) {
        this.accountAdminService = accountAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_ACCOUNT_MANAGE')")
    public Flux<AccountResponse> listAccounts() {
        return accountAdminService.listAccounts()
                .map(account -> new AccountResponse(account.id(), account.tenantId(), account.username(), account.active(), account.roleCodes(), account.permissionCodes(), account.dataScope()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_ACCOUNT_MANAGE')")
    public Mono<ApiResponse<AccountResponse>> createAccount(@RequestBody Mono<@Valid AccountCreateRequest> requestMono) {
        return requestMono.flatMap(request -> accountAdminService.createAccount(request.tenantId(), request.username(), request.password(), request.roleCodes()))
                .map(account -> new AccountResponse(account.id(), account.tenantId(), account.username(), account.active(), account.roleCodes(), account.permissionCodes(), account.dataScope()))
                .map(ApiResponse::success);
    }

    @PutMapping("/{accountId}/roles")
    @PreAuthorize("hasAuthority('PERM_ACCOUNT_MANAGE')")
    public Mono<ApiResponse<AccountResponse>> updateRoles(@PathVariable Long accountId, @RequestBody Mono<@Valid AccountRoleUpdateRequest> requestMono) {
        return requestMono.flatMap(request -> accountAdminService.updateRoles(accountId, request.roleCodes()))
                .map(account -> new AccountResponse(account.id(), account.tenantId(), account.username(), account.active(), account.roleCodes(), account.permissionCodes(), account.dataScope()))
                .map(ApiResponse::success);
    }

    @PutMapping("/{accountId}/toggle")
    @PreAuthorize("hasAuthority('PERM_ACCOUNT_MANAGE')")
    public Mono<ApiResponse<AccountResponse>> toggle(@PathVariable Long accountId, @RequestBody Mono<AccountToggleRequest> requestMono) {
        return requestMono.defaultIfEmpty(new AccountToggleRequest(true))
                .flatMap(request -> accountAdminService.toggleActive(accountId, request.active()))
                .map(account -> new AccountResponse(account.id(), account.tenantId(), account.username(), account.active(), account.roleCodes(), account.permissionCodes(), account.dataScope()))
                .map(ApiResponse::success);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('PERM_ACCOUNT_MANAGE')")
    public Flux<RoleResponse> listRoles() {
        return accountAdminService.listRoles()
                .map(this::mapRole);
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('PERM_ACCOUNT_MANAGE')")
    public Mono<ApiResponse<RoleResponse>> createRole(@RequestBody Mono<@Valid RoleMutationRequest> requestMono) {
        return requestMono.flatMap(request -> accountAdminService.createRole(request.code(), request.name(), request.dataScope(), request.permissionCodes()))
                .map(this::mapRole)
                .map(ApiResponse::success);
    }

    @PutMapping("/roles/{code}")
    @PreAuthorize("hasAuthority('PERM_ACCOUNT_MANAGE')")
    public Mono<ApiResponse<RoleResponse>> updateRole(@PathVariable String code, @RequestBody Mono<@Valid RoleMutationRequest> requestMono) {
        return requestMono.flatMap(request -> accountAdminService.updateRole(code, request.name(), request.dataScope(), request.permissionCodes()))
                .map(this::mapRole)
                .map(ApiResponse::success);
    }

    private RoleResponse mapRole(Role role) {
        return new RoleResponse(role.code(), role.name(), role.dataScope(), role.permissionCodes());
    }
}
