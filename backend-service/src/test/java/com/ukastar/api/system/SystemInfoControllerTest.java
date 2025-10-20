package com.ukastar.api.system;

import com.ukastar.api.system.vo.SystemEchoRequest;
import com.ukastar.api.system.vo.SystemEchoResponse;
import com.ukastar.api.system.vo.SystemInfoResponse;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemInfoControllerTest extends WebTestClientSupport {

    private static final ParameterizedTypeReference<ApiResponse<SystemInfoResponse>> INFO_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<SystemEchoResponse>> ECHO_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Test
    void shouldExposeSystemInfoAndEchoEndpoints() {
        String accessToken = loginAsPlatformAdmin().accessToken();
        WebTestClient authorizedClient = withBearerToken(accessToken);

        WebTestClient.ResponseSpec infoResponse = authorizedClient.get()
                .uri("/api/system/info")
                .exchange();
        infoResponse.expectStatus().isOk();
        ApiResponse<SystemInfoResponse> infoBody = readBody(infoResponse, INFO_RESPONSE_TYPE);
        assertNotNull(infoBody);
        assertEquals(ApiResponse.SUCCESS_CODE, infoBody.code());
        SystemInfoResponse info = infoBody.data();
        assertNotNull(info);
        assertEquals("backend-service", info.applicationName());
        assertNotNull(info.version());
        assertNotNull(info.buildTime());

        String message = "hello-webflux";
        WebTestClient.ResponseSpec echoResponse = authorizedClient.post()
                .uri("/api/system/echo")
                .bodyValue(new SystemEchoRequest(message))
                .exchange();
        echoResponse.expectStatus().isOk();
        ApiResponse<SystemEchoResponse> echoBody = readBody(echoResponse, ECHO_RESPONSE_TYPE);
        assertNotNull(echoBody);
        assertEquals(ApiResponse.SUCCESS_CODE, echoBody.code());
        assertEquals(message, echoBody.data().message());
        assertTrue(echoBody.timestamp().isAfter(infoBody.timestamp()) || echoBody.timestamp().equals(infoBody.timestamp()));
    }
}
