package com.ukastar.api.auth;

import com.ukastar.api.auth.vo.LoginRequest;
import com.ukastar.api.auth.vo.RefreshTokenRequest;
import com.ukastar.api.auth.vo.TokenResponse;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthControllerTest extends WebTestClientSupport {

    private static final ParameterizedTypeReference<ApiResponse<TokenResponse>> TOKEN_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<Void>> VOID_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Test
    void loginRefreshAndLogoutFlowShouldSucceed() {
        WebTestClient.ResponseSpec loginResponse = webTestClient.post()
                .uri("/api/auth/login")
                .bodyValue(new LoginRequest(1L, "platform-admin", "Admin@123"))
                .exchange();
        loginResponse.expectStatus().isOk();
        ApiResponse<TokenResponse> loginBody = readBody(loginResponse, TOKEN_RESPONSE_TYPE);
        assertNotNull(loginBody);
        assertEquals(ApiResponse.SUCCESS_CODE, loginBody.code());
        TokenResponse initialTokens = loginBody.data();
        assertNotNull(initialTokens);
        assertNotNull(initialTokens.accessToken());
        assertNotNull(initialTokens.refreshToken());

        WebTestClient.ResponseSpec refreshResponse = webTestClient.post()
                .uri("/api/auth/refresh")
                .bodyValue(new RefreshTokenRequest(initialTokens.refreshToken()))
                .exchange();
        refreshResponse.expectStatus().isOk();
        ApiResponse<TokenResponse> refreshBody = readBody(refreshResponse, TOKEN_RESPONSE_TYPE);
        assertNotNull(refreshBody);
        assertEquals(initialTokens.accountId(), refreshBody.data().accountId());
        assertEquals(initialTokens.tenantId(), refreshBody.data().tenantId());

        WebTestClient.ResponseSpec logoutResponse = webTestClient.post()
                .uri("/api/auth/logout")
                .bodyValue(new RefreshTokenRequest(refreshBody.data().refreshToken()))
                .exchange();
        logoutResponse.expectStatus().isOk();
        ApiResponse<Void> logoutBody = readBody(logoutResponse, VOID_RESPONSE_TYPE);
        assertNotNull(logoutBody);
        assertEquals(ApiResponse.SUCCESS_CODE, logoutBody.code());
    }
}
