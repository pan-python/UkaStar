package com.ukastar.security.auth;

import com.ukastar.security.model.CurrentUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 携带 JWT 信息的认证对象。
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final CurrentUser principal;
    private final String token;

    private JwtAuthenticationToken(CurrentUser principal,
                                   Collection<? extends GrantedAuthority> authorities,
                                   String token,
                                   boolean authenticated) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        super.setAuthenticated(authenticated);
    }

    public static JwtAuthenticationToken unauthenticated(String token) {
        return new JwtAuthenticationToken(null, null, token, false);
    }

    public static JwtAuthenticationToken authenticated(CurrentUser principal,
                                                       Collection<? extends GrantedAuthority> authorities,
                                                       String token) {
        return new JwtAuthenticationToken(principal, authorities, token, true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public CurrentUser getPrincipal() {
        return principal;
    }
}
