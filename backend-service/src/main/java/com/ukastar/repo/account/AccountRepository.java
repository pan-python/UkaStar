package com.ukastar.repo.account;

import com.ukastar.domain.account.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 账号仓储抽象，后续可切换为 MyBatis-Plus 实现。
 */
public interface AccountRepository {

    Mono<Account> findByTenantIdAndUsername(Long tenantId, String username);

    Mono<Account> findById(Long id);

    Flux<Account> findAll();

    Mono<Account> save(Account account);
}
