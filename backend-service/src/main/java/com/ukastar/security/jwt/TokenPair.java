package com.ukastar.security.jwt;

import java.time.Instant;

/**
 * 登录或刷新后返回的访问令牌与刷新令牌。
 */
public record TokenPair(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {
}
