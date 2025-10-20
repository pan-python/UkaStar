package com.ukastar.api.family;

import com.ukastar.api.family.vo.ChildBindingRequest;
import com.ukastar.api.family.vo.ChildPayload;
import com.ukastar.api.family.vo.FamilyCreateRequest;
import com.ukastar.api.family.vo.FamilyResponse;
import com.ukastar.api.family.vo.ParentBindingRequest;
import com.ukastar.api.family.vo.ParentPayload;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FamilyControllerTest extends WebTestClientSupport {

    private static final ParameterizedTypeReference<ApiResponse<FamilyResponse>> FAMILY_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Test
    void createAndBindFamilyMembersShouldSucceed() {
        WebTestClient client = withBearerToken(loginAsPlatformAdmin().accessToken());

        ApiResponse<FamilyResponse> createFamily = readBody(client.post()
                .uri("/api/families")
                .bodyValue(new FamilyCreateRequest(1L, "测试家庭"))
                .exchange(), FAMILY_RESPONSE_TYPE);
        FamilyResponse family = createFamily.data();
        assertEquals("测试家庭", family.familyName());

        ParentPayload parent = new ParentPayload(null, 1L, "家长A", "13900000001");
        ApiResponse<FamilyResponse> afterParents = readBody(client.post()
                .uri("/api/families/{id}/parents", family.id())
                .bodyValue(new ParentBindingRequest(List.of(parent)))
                .exchange(), FAMILY_RESPONSE_TYPE);
        assertEquals(1, afterParents.data().parents().size());

        ChildPayload child = new ChildPayload(null, 1L, "孩子A", Instant.parse("2020-01-01T00:00:00Z"));
        ApiResponse<FamilyResponse> afterChildren = readBody(client.post()
                .uri("/api/families/{id}/children", family.id())
                .bodyValue(new ChildBindingRequest(List.of(child)))
                .exchange(), FAMILY_RESPONSE_TYPE);
        assertEquals(1, afterChildren.data().children().size());

        List<FamilyResponse> families = client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/families").queryParam("tenantId", 1).build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FamilyResponse.class)
                .returnResult()
                .getResponseBody();
        assertFalse(families.isEmpty());
    }
}
