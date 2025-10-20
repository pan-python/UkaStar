package com.ukastar.infra.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ukastar.domain.account.Account;
import com.ukastar.domain.rbac.DataScope;
import com.ukastar.infra.rbac.InMemoryRbacStore;
import com.ukastar.persistence.entity.AccountEntity;
import com.ukastar.persistence.entity.AccountRoleEntity;
import com.ukastar.persistence.entity.RoleEntity;
import com.ukastar.persistence.mapper.AccountMapper;
import com.ukastar.persistence.mapper.AccountRoleMapper;
import com.ukastar.persistence.mapper.RoleMapper;
import com.ukastar.repo.account.AccountRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 MyBatis-Plus 的账号仓储实现。
 */
@Repository
@ConditionalOnProperty(prefix = "infra.db", name = "enabled", havingValue = "true")
public class MpAccountRepository implements AccountRepository {

    private final AccountMapper accountMapper;
    private final RoleMapper roleMapper;
    private final AccountRoleMapper accountRoleMapper;
    private final InMemoryRbacStore rbacStore;

    public MpAccountRepository(AccountMapper accountMapper,
                               RoleMapper roleMapper,
                               AccountRoleMapper accountRoleMapper,
                               InMemoryRbacStore rbacStore) {
        this.accountMapper = accountMapper;
        this.roleMapper = roleMapper;
        this.accountRoleMapper = accountRoleMapper;
        this.rbacStore = rbacStore;
    }

    @Override
    public Mono<Account> findByTenantIdAndUsername(Long tenantId, String username) {
        return Mono.fromCallable(() -> accountMapper.selectOne(new QueryWrapper<AccountEntity>()
                        .eq("tenant_id", tenantId)
                        .eq("username", username)))
                .flatMap(entity -> entity == null ? Mono.empty() : loadRolesAndBuild(entity));
    }

    @Override
    public Mono<Account> findById(Long id) {
        return Mono.fromCallable(() -> accountMapper.selectById(id))
                .flatMap(entity -> entity == null ? Mono.empty() : loadRolesAndBuild(entity));
    }

    @Override
    public Flux<Account> findAll() {
        return Flux.defer(() -> Flux.fromIterable(accountMapper.selectList(null)))
                .flatMap(this::loadRolesAndBuild);
    }

    @Override
    public Mono<Account> save(Account account) {
        return Mono.fromCallable(() -> {
            AccountEntity entity = new AccountEntity();
            entity.setId(account.id());
            entity.setTenantId(account.tenantId());
            entity.setUsername(account.username());
            entity.setPasswordHash(account.passwordHash());
            entity.setStatus(account.active() ? 1 : 0);
            entity.setTokenVersion(account.tokenVersion());
            if (entity.getId() == null) {
                accountMapper.insert(entity);
            } else {
                accountMapper.updateById(entity);
            }
            return entity.getId();
        }).flatMap(this::findById);
    }

    private Mono<Account> loadRolesAndBuild(AccountEntity entity) {
        return Mono.fromCallable(() -> {
            // 查询账号关联角色
            var binds = accountRoleMapper.selectList(new QueryWrapper<AccountRoleEntity>()
                    .eq("tenant_id", entity.getTenantId())
                    .eq("account_id", entity.getId()));
            Set<Long> roleIds = binds.stream().map(AccountRoleEntity::getRoleId).collect(Collectors.toSet());
            Set<String> roleCodes = roleIds.isEmpty() ? Set.of() : roleMapper.selectBatchIds(roleIds).stream()
                    .map(RoleEntity::getCode).collect(Collectors.toUnmodifiableSet());

            // 基于内存 RBAC 解析权限与数据范围（后续可切换 DB 权限）
            var permissionCodes = rbacStore.permissionCodesForRoles(roleCodes);
            var scope = rbacStore.resolveDataScope(roleCodes, permissionCodes);
            var authorities = rbacStore.authoritiesFor(roleCodes, permissionCodes);

            return new Account(
                    entity.getId(),
                    entity.getTenantId(),
                    entity.getUsername(),
                    entity.getPasswordHash(),
                    roleCodes,
                    permissionCodes,
                    authorities,
                    scope == null ? DataScope.SELF : scope,
                    entity.getStatus() != null && entity.getStatus() == 1,
                    entity.getTokenVersion() == null ? 0L : entity.getTokenVersion()
            );
        });
    }
}

