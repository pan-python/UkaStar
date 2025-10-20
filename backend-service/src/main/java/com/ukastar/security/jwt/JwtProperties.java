package com.ukastar.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * JWT 相关配置项。
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        @DefaultValue("ukastar-backend") String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl
) {
}
