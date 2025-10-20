package com.ukastar.api.export;

import com.ukastar.api.export.vo.ExportRequest;
import com.ukastar.api.export.vo.ExportResponse;
import com.ukastar.api.family.vo.FamilyCreateRequest;
import com.ukastar.api.family.vo.FamilyResponse;
import com.ukastar.api.points.vo.PointBalanceResponse;
import com.ukastar.api.points.vo.PointOperationRequest;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ExportControllerTest extends WebTestClientSupport {

    private static final ParameterizedTypeReference<ApiResponse<FamilyResponse>> FAMILY_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<ExportResponse>> EXPORT_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Test
    void exportPointsShouldReturnBase64Csv() {
        var token = loginAsPlatformAdmin();
        WebTestClient client = withBearerToken(token.accessToken());

        FamilyResponse family = readBody(client.post()
                .uri("/api/families")
                .bodyValue(new FamilyCreateRequest(token.tenantId(), "导出家庭"))
                .exchange(), FAMILY_RESPONSE_TYPE).data();

        readBody(client.post()
                .uri("/api/points/award")
                .bodyValue(new PointOperationRequest(family.id(), token.accountId(), 8, "导出测试"))
                .exchange(), new ParameterizedTypeReference<ApiResponse<PointBalanceResponse>>() {
        });

        ApiResponse<ExportResponse> export = readBody(client.post()
                .uri("/api/export/points")
                .bodyValue(new ExportRequest(token.tenantId(), "points"))
                .exchange(), EXPORT_RESPONSE_TYPE);
        assertEquals("points-export.csv", export.data().fileName());
        assertFalse(export.data().contentBase64().isBlank());
    }
}
