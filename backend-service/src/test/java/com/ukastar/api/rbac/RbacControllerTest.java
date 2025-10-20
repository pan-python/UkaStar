package com.ukastar.api.rbac;

import com.ukastar.api.rbac.vo.MenuNodeResponse;
import com.ukastar.api.rbac.vo.RbacProfileResponse;
import com.ukastar.api.auth.vo.TokenResponse;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RbacControllerTest extends WebTestClientSupport {

    private static final ParameterizedTypeReference<ApiResponse<RbacProfileResponse>> PROFILE_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Test
    void shouldReturnCurrentUserProfile() {
        TokenResponse session = loginAsPlatformAdmin();
        WebTestClient authorizedClient = withBearerToken(session.accessToken());

        WebTestClient.ResponseSpec response = authorizedClient.get()
                .uri("/api/rbac/profile")
                .exchange();
        response.expectStatus().isOk();
        ApiResponse<RbacProfileResponse> body = readBody(response, PROFILE_RESPONSE_TYPE);
        assertNotNull(body);
        assertEquals(ApiResponse.SUCCESS_CODE, body.code());
        RbacProfileResponse profile = body.data();
        assertNotNull(profile);
        assertEquals(session.accountId(), profile.accountId());
        assertEquals(session.username(), profile.username());
        assertFalse(profile.roles().isEmpty());
        assertFalse(profile.apiPermissions().isEmpty());
        List<MenuNodeResponse> menus = profile.menus();
        assertFalse(menus.isEmpty());
        MenuNodeResponse systemMenu = menus.stream()
                .filter(menu -> "system".equals(menu.code()))
                .findFirst()
                .orElseThrow();
        assertFalse(systemMenu.children().isEmpty());
    }
}
