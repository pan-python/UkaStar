package com.ukastar.api.tenant;

import com.ukastar.api.tenant.vo.TenantCreateRequest;
import com.ukastar.api.tenant.vo.TenantResponse;
import com.ukastar.api.tenant.vo.TenantUpdateRequest;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantControllerTest extends WebTestClientSupport {

    private static final ParameterizedTypeReference<ApiResponse<TenantResponse>> TENANT_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private static final ParameterizedTypeReference<ApiResponse<Void>> VOID_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Test
    void tenantCrudFlowShouldWork() {
        String accessToken = loginAsPlatformAdmin().accessToken();
        WebTestClient client = withBearerToken(accessToken);

        TenantCreateRequest createRequest = new TenantCreateRequest("test-tenant", "测试租户", "联系人", "13200000000");
        ApiResponse<TenantResponse> createResponse = readBody(client.post()
                .uri("/api/tenants")
                .bodyValue(createRequest)
                .exchange(), TENANT_RESPONSE_TYPE);
        assertEquals(ApiResponse.SUCCESS_CODE, createResponse.code());
        TenantResponse created = createResponse.data();
        assertEquals("测试租户", created.name());

        List<TenantResponse> tenants = client.get()
                .uri("/api/tenants")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TenantResponse.class)
                .returnResult()
                .getResponseBody();
        assertTrue(tenants.stream().anyMatch(t -> t.code().equals("test-tenant")));

        ApiResponse<TenantResponse> updated = readBody(client.put()
                .uri("/api/tenants/{id}", created.id())
                .bodyValue(new TenantUpdateRequest("测试租户更新", "新联系人", "13300000000"))
                .exchange(), TENANT_RESPONSE_TYPE);
        assertEquals("测试租户更新", updated.data().name());

        ApiResponse<TenantResponse> suspended = readBody(client.post()
                .uri("/api/tenants/{id}/suspend", created.id())
                .exchange(), TENANT_RESPONSE_TYPE);
        assertEquals("SUSPENDED", suspended.data().status().name());

        ApiResponse<TenantResponse> activated = readBody(client.post()
                .uri("/api/tenants/{id}/activate", created.id())
                .exchange(), TENANT_RESPONSE_TYPE);
        assertEquals("ACTIVE", activated.data().status().name());

        ApiResponse<Void> deleteResponse = readBody(client.delete()
                .uri("/api/tenants/{id}", created.id())
                .exchange(), VOID_RESPONSE_TYPE);
        assertEquals(ApiResponse.SUCCESS_CODE, deleteResponse.code());
    }
}
