package com.ukastar.security.jwt;

import java.time.Instant;
import java.util.Set;

/**
 * 解析后的令牌载荷信息。
 */
public record TokenDetails(
        String token,
        Long accountId,
        Long tenantId,
        String username,
        Set<String> authorities,
        long tokenVersion,
        TokenType tokenType,
        Instant issuedAt,
        Instant expiresAt
) {
}
