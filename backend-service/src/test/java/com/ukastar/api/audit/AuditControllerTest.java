package com.ukastar.api.audit;

import com.ukastar.api.audit.vo.AuditEventResponse;
import com.ukastar.api.audit.vo.AuditRecordRequest;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AuditControllerTest extends WebTestClientSupport {

    private static final ParameterizedTypeReference<ApiResponse<AuditEventResponse>> AUDIT_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Test
    void auditRecordAndQueryShouldSucceed() {
        var token = loginAsPlatformAdmin();
        WebTestClient client = withBearerToken(token.accessToken());

        ApiResponse<AuditEventResponse> record = readBody(client.post()
                .uri("/api/audit")
                .bodyValue(new AuditRecordRequest(token.tenantId(), "LOGIN", "platform-admin", "system", "登录成功"))
                .exchange(), AUDIT_RESPONSE_TYPE);
        assertEquals("LOGIN", record.data().eventType());

        List<AuditEventResponse> tenantEvents = client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/audit").queryParam("tenantId", token.tenantId()).build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditEventResponse.class)
                .returnResult()
                .getResponseBody();
        assertFalse(tenantEvents.isEmpty());

        List<AuditEventResponse> typeEvents = client.get()
                .uri("/api/audit/type/{eventType}", "LOGIN")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuditEventResponse.class)
                .returnResult()
                .getResponseBody();
        assertFalse(typeEvents.isEmpty());
    }
}
