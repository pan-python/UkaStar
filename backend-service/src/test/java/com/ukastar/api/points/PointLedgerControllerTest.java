package com.ukastar.api.points;

import com.ukastar.api.family.vo.*;
import com.ukastar.api.points.vo.PointBalanceResponse;
import com.ukastar.api.points.vo.PointOperationRequest;
import com.ukastar.api.points.vo.PointRecordResponse;
import com.ukastar.api.points.vo.PointStatisticsResponse;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PointLedgerControllerTest extends WebTestClientSupport {

    private static final ParameterizedTypeReference<ApiResponse<PointBalanceResponse>> BALANCE_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<FamilyResponse>> FAMILY_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Test
    void pointsOperationsAndStatisticsShouldWork() {
        var token = loginAsPlatformAdmin();
        WebTestClient client = withBearerToken(token.accessToken());

        ApiResponse<FamilyResponse> familyCreate = readBody(client.post()
                .uri("/api/families")
                .bodyValue(new FamilyCreateRequest(token.tenantId(), "积分家庭"))
                .exchange(), FAMILY_RESPONSE_TYPE);
        FamilyResponse family = familyCreate.data();

        // 绑定一个孩子
        ApiResponse<FamilyResponse> afterChildren = readBody(client.post()
                .uri("/api/families/" + family.id() + "/children")
                .bodyValue(new ChildBindingRequest(List.of(new ChildPayload(null, token.tenantId(), "小明", null))))
                .exchange(), FAMILY_RESPONSE_TYPE);
        Long childId = afterChildren.data().children().get(0).id();

        ApiResponse<PointBalanceResponse> award = readBody(client.post()
                .uri("/api/points/award")
                .bodyValue(new PointOperationRequest(childId, token.accountId(), 20, "课堂表现"))
                .exchange(), BALANCE_RESPONSE_TYPE);
        assertEquals(20, award.data().balance());

        ApiResponse<PointBalanceResponse> deduct = readBody(client.post()
                .uri("/api/points/deduct")
                .bodyValue(new PointOperationRequest(childId, token.accountId(), 5, "迟到"))
                .exchange(), BALANCE_RESPONSE_TYPE);
        assertEquals(15, deduct.data().balance());

        ApiResponse<PointBalanceResponse> redeem = readBody(client.post()
                .uri("/api/points/redeem")
                .bodyValue(new PointOperationRequest(childId, token.accountId(), 10, "兑换礼物"))
                .exchange(), BALANCE_RESPONSE_TYPE);
        assertEquals(5, redeem.data().balance());

        List<PointRecordResponse> records = client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/points/records").queryParam("childId", childId).build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PointRecordResponse.class)
                .returnResult()
                .getResponseBody();
        assertFalse(records.isEmpty());

        PointStatisticsResponse stats = client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/points/statistics").queryParam("tenantId", token.tenantId()).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PointStatisticsResponse.class)
                .returnResult()
                .getResponseBody();
        assertEquals(token.tenantId(), award.data().tenantId());
        assertFalse(stats.totalPoints() < 0);
    }
}
