package com.ukastar.api.points;

import com.ukastar.api.family.vo.ChildBindingRequest;
import com.ukastar.api.family.vo.ChildPayload;
import com.ukastar.api.family.vo.FamilyCreateRequest;
import com.ukastar.api.family.vo.FamilyResponse;
import com.ukastar.api.points.vo.PointBalanceResponse;
import com.ukastar.api.points.vo.PointOperationRequest;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PointsBalanceControllerTest extends WebTestClientSupport {

    private static final ParameterizedTypeReference<ApiResponse<FamilyResponse>> FAMILY_RESPONSE_TYPE = new ParameterizedTypeReference<>(){};
    private static final ParameterizedTypeReference<ApiResponse<PointBalanceResponse>> BALANCE_RESPONSE_TYPE = new ParameterizedTypeReference<>(){};

    @Test
    void balanceShouldReflectLatestAfterPoints() {
        var token = loginAsPlatformAdmin();
        WebTestClient client = withBearerToken(token.accessToken());

        Long tenantId = token.tenantId();
        ApiResponse<FamilyResponse> familyCreate = readBody(client.post().uri("/api/families").bodyValue(new FamilyCreateRequest(tenantId, "测试家庭")).exchange(), FAMILY_RESPONSE_TYPE);
        Long familyId = familyCreate.data().id();
        ApiResponse<FamilyResponse> afterChildren = readBody(client.post().uri("/api/families/"+familyId+"/children").bodyValue(new ChildBindingRequest(List.of(new ChildPayload(null, tenantId, "小明", null)))).exchange(), FAMILY_RESPONSE_TYPE);
        Long childId = afterChildren.data().children().get(0).id();

        readBody(client.post().uri("/api/points/award").bodyValue(new PointOperationRequest(childId, token.accountId(), 12, "表现")).exchange(), BALANCE_RESPONSE_TYPE);
        readBody(client.post().uri("/api/points/deduct").bodyValue(new PointOperationRequest(childId, token.accountId(), 2, "迟到")).exchange(), BALANCE_RESPONSE_TYPE);

        ApiResponse<PointBalanceResponse> bal = readBody(client.get().uri(uriBuilder -> uriBuilder.path("/api/points/balance").queryParam("childId", childId).build()).exchange(), BALANCE_RESPONSE_TYPE);
        assertEquals(10, bal.data().balance());
    }
}

