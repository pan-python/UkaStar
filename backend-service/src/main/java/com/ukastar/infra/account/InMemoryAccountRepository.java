package com.ukastar.infra.account;

import com.ukastar.domain.account.Account;
import com.ukastar.domain.rbac.DataScope;
import com.ukastar.infra.rbac.InMemoryRbacStore;
import com.ukastar.repo.account.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于内存的账号仓储实现，便于在接入数据库前验证鉴权流程。
 */
@Repository
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<Long, Account> accounts = new ConcurrentHashMap<>();
    private final InMemoryRbacStore rbacStore;
    private final PasswordEncoder passwordEncoder;
    private final AtomicLong sequence = new AtomicLong(10);

    public InMemoryAccountRepository(PasswordEncoder passwordEncoder, InMemoryRbacStore rbacStore) {
        this.rbacStore = rbacStore;
        this.passwordEncoder = passwordEncoder;
        registerAccount(1L, 1L, "platform-admin", passwordEncoder.encode("Admin@123"), Set.of("PLATFORM_ADMIN"), Set.of(), true, 0L);
        registerAccount(2L, 1L, "tenant-operator", passwordEncoder.encode("Operator@123"), Set.of("TENANT_OPERATOR"), Set.of(), true, 0L);
        registerAccount(3L, 1L, "tenant-viewer", passwordEncoder.encode("Viewer@123"), Set.of("TENANT_VIEWER"), Set.of(), true, 0L);
        sequence.compareAndSet(10, 3L);
    }

    @Override
    public Mono<Account> findByTenantIdAndUsername(Long tenantId, String username) {
        return Mono.justOrEmpty(
                accounts.values().stream()
                        .filter(account -> account.tenantId().equals(tenantId))
                        .filter(account -> account.username().equalsIgnoreCase(username))
                        .findFirst()
        );
    }

    @Override
    public Mono<Account> findById(Long id) {
        return Mono.justOrEmpty(Optional.ofNullable(accounts.get(id)));
    }

    @Override
    public Flux<Account> findAll() {
        return Flux.fromIterable(accounts.values()).sort((a, b) -> a.id().compareTo(b.id()));
    }

    @Override
    public Mono<Account> save(Account account) {
        Account persisted = account;
        if (account.id() == null) {
            long id = sequence.incrementAndGet();
            persisted = enrichAuthorities(new Account(id, account.tenantId(), account.username(), account.passwordHash(), account.roleCodes(), account.permissionCodes(), account.authorities(), account.dataScope(), account.active(), account.tokenVersion()));
        } else {
            persisted = enrichAuthorities(account);
        }
        accounts.put(persisted.id(), persisted);
        return Mono.just(persisted);
    }

    private Account enrichAuthorities(Account source) {
        Set<String> permissions = new HashSet<>(rbacStore.permissionCodesForRoles(source.roleCodes()));
        if (source.permissionCodes() != null) {
            permissions.addAll(source.permissionCodes());
        }
        DataScope scope = rbacStore.resolveDataScope(source.roleCodes(), permissions);
        Set<String> authorities = rbacStore.authoritiesFor(source.roleCodes(), permissions);
        return new Account(source.id(), source.tenantId(), source.username(), source.passwordHash(), Set.copyOf(source.roleCodes()), Set.copyOf(permissions), authorities, scope, source.active(), source.tokenVersion());
    }

    private void registerAccount(Long id,
                                 Long tenantId,
                                 String username,
                                 String passwordHash,
                                 Set<String> roleCodes,
                                 Set<String> directPermissionCodes,
                                 boolean active,
                                 long tokenVersion) {
        Set<String> permissions = new HashSet<>(rbacStore.permissionCodesForRoles(roleCodes));
        permissions.addAll(directPermissionCodes);
        DataScope scope = rbacStore.resolveDataScope(roleCodes, permissions);
        Set<String> authorities = rbacStore.authoritiesFor(roleCodes, permissions);
        Account account = new Account(
                id,
                tenantId,
                username,
                passwordHash,
                Set.copyOf(roleCodes),
                Set.copyOf(permissions),
                authorities,
                scope,
                active,
                tokenVersion
        );
        accounts.put(id, account);
    }

    public Mono<Account> createAccount(Long tenantId, String username, String rawPassword, Set<String> roleCodes) {
        String hash = passwordEncoder.encode(rawPassword);
        Account account = new Account(null, tenantId, username, hash, Set.copyOf(roleCodes), Set.of(), Set.of(), DataScope.SELF, true, 0L);
        return save(account);
    }
}
