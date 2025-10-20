package com.ukastar.service.account;

import com.ukastar.domain.account.Account;
import com.ukastar.domain.rbac.DataScope;
import com.ukastar.domain.rbac.Role;
import com.ukastar.infra.rbac.InMemoryRbacStore;
import com.ukastar.repo.account.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * 默认账号与角色管理实现。
 */
@Service
public class DefaultAccountAdminService implements AccountAdminService {

    private final AccountRepository accountRepository;
    private final InMemoryRbacStore rbacStore;
    private final PasswordEncoder passwordEncoder;

    public DefaultAccountAdminService(AccountRepository accountRepository,
                                      InMemoryRbacStore rbacStore,
                                      PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.rbacStore = rbacStore;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Flux<Account> listAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Mono<Account> createAccount(Long tenantId, String username, String rawPassword, Set<String> roleCodes) {
        Account account = new Account(null, tenantId, username, passwordEncoder.encode(rawPassword), Set.copyOf(roleCodes), Set.of(), Set.of(), DataScope.SELF, true, 0L);
        return accountRepository.save(account);
    }

    @Override
    public Mono<Account> updateRoles(Long accountId, Set<String> roleCodes) {
        return accountRepository.findById(accountId)
                .map(existing -> new Account(existing.id(), existing.tenantId(), existing.username(), existing.passwordHash(), Set.copyOf(roleCodes), existing.permissionCodes(), existing.authorities(), existing.dataScope(), existing.active(), existing.tokenVersion()))
                .flatMap(accountRepository::save);
    }

    @Override
    public Mono<Account> toggleActive(Long accountId, boolean active) {
        return accountRepository.findById(accountId)
                .map(existing -> new Account(existing.id(), existing.tenantId(), existing.username(), existing.passwordHash(), existing.roleCodes(), existing.permissionCodes(), existing.authorities(), existing.dataScope(), active, existing.tokenVersion()))
                .flatMap(accountRepository::save);
    }

    @Override
    public Flux<Role> listRoles() {
        return Flux.fromIterable(rbacStore.listRoles());
    }

    @Override
    public Mono<Role> createRole(String code, String name, DataScope scope, Set<String> permissionCodes) {
        return Mono.fromSupplier(() -> rbacStore.createRole(code, name, scope, permissionCodes));
    }

    @Override
    public Mono<Role> updateRole(String code, String name, DataScope scope, Set<String> permissionCodes) {
        return Mono.fromSupplier(() -> rbacStore.updateRole(code, name, scope, permissionCodes));
    }
}
