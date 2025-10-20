package com.ukastar.security.support;

import com.ukastar.common.error.ErrorCode;
import com.ukastar.common.exception.BusinessException;
import com.ukastar.security.model.CurrentUser;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 基于 Spring Security 上下文的当前用户提供者。
 */
@Component
@Primary
public class JwtCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Mono<CurrentUser> currentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(CurrentUser.class::isInstance)
                .cast(CurrentUser.class)
                .switchIfEmpty(Mono.error(new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或登录已失效")));
    }
}
