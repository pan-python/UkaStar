package com.ukastar.security.auth;

import com.ukastar.domain.account.Account;
import com.ukastar.repo.account.AccountRepository;
import com.ukastar.security.jwt.JwtService;
import com.ukastar.security.jwt.TokenDetails;
import com.ukastar.security.jwt.TokenType;
import com.ukastar.security.model.CurrentUser;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 负责将 JWT 转换为经过校验的认证上下文。
 */
@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final Clock clock;

    public JwtAuthenticationManager(JwtService jwtService, AccountRepository accountRepository, Clock clock) {
        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
        this.clock = clock;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            return Mono.error(new BadCredentialsException("Unsupported authentication type"));
        }
        String token = Objects.toString(jwtAuthenticationToken.getCredentials(), null);
        if (token == null) {
            return Mono.error(new BadCredentialsException("Missing token"));
        }
        return Mono.fromCallable(() -> jwtService.parse(token))
                .onErrorMap(ex -> new BadCredentialsException("Invalid token", ex))
                .flatMap(details -> validateAndBuildAuthentication(token, details));
    }

    private Mono<Authentication> validateAndBuildAuthentication(String token, TokenDetails details) {
        if (details.tokenType() != TokenType.ACCESS) {
            return Mono.error(new BadCredentialsException("Access token required"));
        }
        if (details.expiresAt().isBefore(clock.instant())) {
            return Mono.error(new BadCredentialsException("Token expired"));
        }
        return accountRepository.findById(details.accountId())
                .switchIfEmpty(Mono.error(new BadCredentialsException("Account not found")))
                .map(account -> ensureTokenIsValid(account, details))
                .map(account -> JwtAuthenticationToken.authenticated(
                        new CurrentUser(
                                account.id(),
                                account.tenantId(),
                                account.username(),
                                account.roleCodes(),
                                account.permissionCodes(),
                                account.authorities(),
                                account.dataScope()
                        ),
                        account.authorities().stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toUnmodifiableSet()),
                        token
                ));
    }

    private Account ensureTokenIsValid(Account account, TokenDetails details) {
        if (!account.active()) {
            throw new BadCredentialsException("Account disabled");
        }
        if (account.tokenVersion() != details.tokenVersion()) {
            throw new BadCredentialsException("Token revoked");
        }
        if (!account.tenantId().equals(details.tenantId())) {
            throw new BadCredentialsException("Tenant mismatch");
        }
        if (!account.username().equals(details.username())) {
            throw new BadCredentialsException("Username mismatch");
        }
        return account;
    }
}
