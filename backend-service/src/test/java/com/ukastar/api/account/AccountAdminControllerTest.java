package com.ukastar.api.account;

import com.ukastar.api.account.vo.AccountCreateRequest;
import com.ukastar.api.account.vo.AccountResponse;
import com.ukastar.api.account.vo.AccountRoleUpdateRequest;
import com.ukastar.api.account.vo.AccountToggleRequest;
import com.ukastar.api.account.vo.RoleMutationRequest;
import com.ukastar.api.account.vo.RoleResponse;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.domain.rbac.DataScope;
import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountAdminControllerTest extends WebTestClientSupport {

    private static final ParameterizedTypeReference<ApiResponse<AccountResponse>> ACCOUNT_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<RoleResponse>> ROLE_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Test
    void accountAndRoleManagementShouldSucceed() {
        String accessToken = loginAsPlatformAdmin().accessToken();
        WebTestClient client = withBearerToken(accessToken);

        ApiResponse<RoleResponse> roleCreate = readBody(client.post()
                .uri("/api/accounts/roles")
                .bodyValue(new RoleMutationRequest("POINT_AUDITOR", "积分审计", DataScope.GROUP, Set.of("PERM_POINTS_VIEW", "PERM_AUDIT_VIEW")))
                .exchange(), ROLE_RESPONSE_TYPE);
        assertEquals(ApiResponse.SUCCESS_CODE, roleCreate.code());
        RoleResponse roleResponse = roleCreate.data();
        assertEquals("POINT_AUDITOR", roleResponse.code());

        ApiResponse<AccountResponse> accountCreate = readBody(client.post()
                .uri("/api/accounts")
                .bodyValue(new AccountCreateRequest(1L, "auditor", "Auditor@123", Set.of("POINT_AUDITOR")))
                .exchange(), ACCOUNT_RESPONSE_TYPE);
        assertEquals(ApiResponse.SUCCESS_CODE, accountCreate.code());
        AccountResponse account = accountCreate.data();
        assertEquals("auditor", account.username());

        ApiResponse<AccountResponse> updatedRoles = readBody(client.put()
                .uri("/api/accounts/{id}/roles", account.id())
                .bodyValue(new AccountRoleUpdateRequest(Set.of("POINT_AUDITOR", "TENANT_VIEWER")))
                .exchange(), ACCOUNT_RESPONSE_TYPE);
        assertTrue(updatedRoles.data().roleCodes().contains("TENANT_VIEWER"));

        ApiResponse<AccountResponse> toggled = readBody(client.put()
                .uri("/api/accounts/{id}/toggle", account.id())
                .bodyValue(new AccountToggleRequest(false))
                .exchange(), ACCOUNT_RESPONSE_TYPE);
        assertEquals(false, toggled.data().active());

        List<RoleResponse> roles = client.get()
                .uri("/api/accounts/roles")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RoleResponse.class)
                .returnResult()
                .getResponseBody();
        assertTrue(roles.stream().anyMatch(role -> role.code().equals("POINT_AUDITOR")));

        List<AccountResponse> accounts = client.get()
                .uri("/api/accounts")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AccountResponse.class)
                .returnResult()
                .getResponseBody();
        assertTrue(accounts.stream().anyMatch(a -> a.username().equals("auditor")));
    }
}
