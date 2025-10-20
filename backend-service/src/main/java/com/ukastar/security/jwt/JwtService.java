package com.ukastar.security.jwt;

import com.ukastar.domain.account.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JWT 令牌生成与解析服务。
 */
@Component
public class JwtService {

    private static final String CLAIM_ACCOUNT_ID = "aid";
    private static final String CLAIM_TENANT_ID = "tid";
    private static final String CLAIM_AUTHORITIES = "auth";
    private static final String CLAIM_TOKEN_VERSION = "ver";
    private static final String CLAIM_TOKEN_TYPE = "typ";

    private final JwtProperties properties;
    private final Clock clock;
    private final Key signingKey;

    public JwtService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair generateTokenPair(Account account) {
        Instant now = clock.instant();
        Instant accessExpiresAt = now.plus(properties.accessTokenTtl());
        Instant refreshExpiresAt = now.plus(properties.refreshTokenTtl());

        String accessToken = buildToken(account, TokenType.ACCESS, now, accessExpiresAt);
        String refreshToken = buildToken(account, TokenType.REFRESH, now, refreshExpiresAt);
        return new TokenPair(accessToken, accessExpiresAt, refreshToken, refreshExpiresAt);
    }

    public TokenDetails parse(String token) {
        Jws<Claims> parsed = Jwts.parser()
                .requireIssuer(properties.issuer())
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);
        Claims claims = parsed.getPayload();
        Long accountId = claims.get(CLAIM_ACCOUNT_ID, Long.class);
        Long tenantId = claims.get(CLAIM_TENANT_ID, Long.class);
        @SuppressWarnings("unchecked")
        List<String> authoritiesClaim = claims.get(CLAIM_AUTHORITIES, List.class);
        Set<String> authorities = authoritiesClaim == null ? Set.of() : Set.copyOf(authoritiesClaim);
        long version = claims.get(CLAIM_TOKEN_VERSION, Long.class);
        TokenType tokenType = TokenType.valueOf(claims.get(CLAIM_TOKEN_TYPE, String.class));
        Instant issuedAt = claims.getIssuedAt().toInstant();
        Instant expiresAt = claims.getExpiration().toInstant();
        String username = claims.getSubject();
        return new TokenDetails(token, accountId, tenantId, username, authorities, version, tokenType, issuedAt, expiresAt);
    }

    private String buildToken(Account account, TokenType tokenType, Instant issuedAt, Instant expiresAt) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_ACCOUNT_ID, account.id());
        claims.put(CLAIM_TENANT_ID, account.tenantId());
        claims.put(CLAIM_AUTHORITIES, List.copyOf(account.authorities()));
        claims.put(CLAIM_TOKEN_VERSION, account.tokenVersion());
        claims.put(CLAIM_TOKEN_TYPE, tokenType.name());

        return Jwts.builder()
                .header().type("JWT").and()
                .claims(claims)
                .issuer(properties.issuer())
                .subject(account.username())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }
}
