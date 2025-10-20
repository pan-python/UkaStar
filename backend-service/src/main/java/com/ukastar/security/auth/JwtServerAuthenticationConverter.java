package com.ukastar.security.auth;

import com.ukastar.security.SecurityConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 将请求头中的 Bearer Token 转换为未认证的 JWT Authentication。
 */
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<JwtAuthenticationToken> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .filter(value -> value.startsWith(SecurityConstants.BEARER_PREFIX))
                .map(value -> value.substring(SecurityConstants.BEARER_PREFIX.length()).trim())
                .filter(token -> !token.isEmpty())
                .map(JwtAuthenticationToken::unauthenticated);
    }
}
