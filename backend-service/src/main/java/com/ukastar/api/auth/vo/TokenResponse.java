package com.ukastar.api.auth.vo;

import com.ukastar.domain.rbac.DataScope;

import java.time.Instant;
import java.util.Set;

/**
 * 登录或刷新后返回的令牌信息。
 */
public record TokenResponse(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        Long accountId,
        Long tenantId,
        String username,
        Set<String> roles,
        Set<String> permissions,
        Set<String> authorities,
        DataScope dataScope
) {
}
