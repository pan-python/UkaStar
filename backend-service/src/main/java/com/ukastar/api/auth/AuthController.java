package com.ukastar.api.auth;

import com.ukastar.api.auth.vo.LoginRequest;
import com.ukastar.api.auth.vo.RefreshTokenRequest;
import com.ukastar.api.auth.vo.TokenResponse;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.service.auth.AuthService;
import com.ukastar.service.auth.AuthSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * 鉴权相关接口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Mono<ApiResponse<TokenResponse>> login(@RequestBody Mono<@Valid LoginRequest> requestMono) {
        return requestMono
                .flatMap(request -> authService.login(request.tenantId(), request.username(), request.password()))
                .map(this::toResponse)
                .map(ApiResponse::success);
    }

    @PostMapping("/refresh")
    public Mono<ApiResponse<TokenResponse>> refresh(@RequestBody Mono<@Valid RefreshTokenRequest> requestMono) {
        return requestMono
                .flatMap(request -> authService.refresh(request.refreshToken()))
                .map(this::toResponse)
                .map(ApiResponse::success);
    }

    @PostMapping("/logout")
    public Mono<ApiResponse<Void>> logout(@RequestBody Mono<@Valid RefreshTokenRequest> requestMono) {
        return requestMono
                .flatMap(request -> authService.logout(request.refreshToken()))
                .thenReturn(ApiResponse.success());
    }

    private TokenResponse toResponse(AuthSession session) {
        var account = session.account();
        var tokenPair = session.tokenPair();
        return new TokenResponse(
                tokenPair.accessToken(),
                tokenPair.accessTokenExpiresAt(),
                tokenPair.refreshToken(),
                tokenPair.refreshTokenExpiresAt(),
                account.id(),
                account.tenantId(),
                account.username(),
                account.roleCodes(),
                account.permissionCodes(),
                account.authorities(),
                account.dataScope()
        );
    }
}
