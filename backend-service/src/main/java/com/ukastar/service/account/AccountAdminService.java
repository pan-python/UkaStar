package com.ukastar.service.account;

import com.ukastar.domain.account.Account;
import com.ukastar.domain.rbac.DataScope;
import com.ukastar.domain.rbac.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * 账号与角色管理服务。
 */
public interface AccountAdminService {

    Flux<Account> listAccounts();

    Mono<Account> createAccount(Long tenantId, String username, String rawPassword, Set<String> roleCodes);

    Mono<Account> updateRoles(Long accountId, Set<String> roleCodes);

    Mono<Account> toggleActive(Long accountId, boolean active);

    Flux<Role> listRoles();

    Mono<Role> createRole(String code, String name, DataScope scope, Set<String> permissionCodes);

    Mono<Role> updateRole(String code, String name, DataScope scope, Set<String> permissionCodes);
}
