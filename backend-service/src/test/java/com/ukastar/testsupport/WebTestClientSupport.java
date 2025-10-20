package com.ukastar.testsupport;

import com.ukastar.api.auth.vo.LoginRequest;
import com.ukastar.api.auth.vo.TokenResponse;
import com.ukastar.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * 基于 WebTestClient 的集成测试基座，提供统一的登录与响应解析工具方法。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public abstract class WebTestClientSupport {

    private static final ParameterizedTypeReference<ApiResponse<TokenResponse>> TOKEN_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Autowired
    protected WebTestClient webTestClient;

    protected TokenResponse loginAsPlatformAdmin() {
        WebTestClient.ResponseSpec response = webTestClient.post()
                .uri("/api/auth/login")
                .bodyValue(new LoginRequest(1L, "platform-admin", "Admin@123"))
                .exchange();
        response.expectStatus().isOk();
        ApiResponse<TokenResponse> body = readBody(response, TOKEN_RESPONSE_TYPE);
        return body.data();
    }

    protected WebTestClient withBearerToken(String accessToken) {
        return webTestClient.mutate()
                .defaultHeaders(headers -> headers.setBearerAuth(accessToken))
                .build();
    }

    protected <T> ApiResponse<T> readBody(WebTestClient.ResponseSpec response,
                                          ParameterizedTypeReference<ApiResponse<T>> typeReference) {
        return response.expectBody(typeReference)
                .returnResult()
                .getResponseBody();
    }
}
