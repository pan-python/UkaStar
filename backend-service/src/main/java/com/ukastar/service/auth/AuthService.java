package com.ukastar.service.auth;

import com.ukastar.common.error.ErrorCode;
import com.ukastar.common.exception.BusinessException;
import com.ukastar.domain.account.Account;
import com.ukastar.repo.account.AccountRepository;
import com.ukastar.security.jwt.JwtService;
import com.ukastar.security.jwt.TokenDetails;
import com.ukastar.security.jwt.TokenType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;

/**
 * 账号鉴权相关的核心服务。
 */
@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Clock clock;

    public AuthService(AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       Clock clock) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.clock = clock;
    }

    public Mono<AuthSession> login(Long tenantId, String username, String password) {
        return accountRepository.findByTenantIdAndUsername(tenantId, username)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误")))
                .flatMap(account -> validatePassword(account, password))
                .map(account -> new AuthSession(account, jwtService.generateTokenPair(account)));
    }

    public Mono<AuthSession> refresh(String refreshToken) {
        return Mono.fromCallable(() -> jwtService.parse(refreshToken))
                .onErrorMap(ex -> new BusinessException(ErrorCode.UNAUTHORIZED, "刷新令牌无效"))
                .flatMap(this::validateRefreshToken)
                .map(account -> new AuthSession(account, jwtService.generateTokenPair(account)));
    }

    public Mono<Void> logout(String refreshToken) {
        return Mono.fromCallable(() -> jwtService.parse(refreshToken))
                .onErrorMap(ex -> new BusinessException(ErrorCode.UNAUTHORIZED, "刷新令牌无效"))
                .flatMap(this::validateRefreshToken)
                .flatMap(account -> accountRepository.save(account.withTokenVersion(account.tokenVersion() + 1)))
                .then();
    }

    private Mono<Account> validatePassword(Account account, String rawPassword) {
        if (!account.active()) {
            return Mono.error(new BusinessException(ErrorCode.FORBIDDEN, "账号已禁用"));
        }
        if (!passwordEncoder.matches(rawPassword, account.passwordHash())) {
            return Mono.error(new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误"));
        }
        return Mono.just(account);
    }

    private Mono<Account> validateRefreshToken(TokenDetails details) {
        if (details.tokenType() != TokenType.REFRESH) {
            return Mono.error(new BusinessException(ErrorCode.UNAUTHORIZED, "刷新令牌类型错误"));
        }
        if (details.expiresAt().isBefore(clock.instant())) {
            return Mono.error(new BusinessException(ErrorCode.UNAUTHORIZED, "刷新令牌已过期"));
        }
        return accountRepository.findById(details.accountId())
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.UNAUTHORIZED, "账号不存在")))
                .flatMap(account -> {
                    if (!account.active()) {
                        return Mono.error(new BusinessException(ErrorCode.FORBIDDEN, "账号已禁用"));
                    }
                    if (account.tokenVersion() != details.tokenVersion()) {
                        return Mono.error(new BusinessException(ErrorCode.UNAUTHORIZED, "刷新令牌已失效"));
                    }
                    if (!account.tenantId().equals(details.tenantId())) {
                        return Mono.error(new BusinessException(ErrorCode.UNAUTHORIZED, "租户不匹配"));
                    }
                    if (!account.username().equals(details.username())) {
                        return Mono.error(new BusinessException(ErrorCode.UNAUTHORIZED, "账号不匹配"));
                    }
                    return Mono.just(account);
                });
    }
}
