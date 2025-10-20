package com.ukastar.api.catalog;

import com.ukastar.api.catalog.vo.CategoryCreateRequest;
import com.ukastar.api.catalog.vo.CategoryRenameRequest;
import com.ukastar.api.catalog.vo.CategoryResponse;
import com.ukastar.api.catalog.vo.PointItemCreateRequest;
import com.ukastar.api.catalog.vo.PointItemResponse;
import com.ukastar.api.catalog.vo.PointItemUpdateRequest;
import com.ukastar.api.catalog.vo.RewardCreateRequest;
import com.ukastar.api.catalog.vo.RewardResponse;
import com.ukastar.api.catalog.vo.RewardUpdateRequest;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.testsupport.WebTestClientSupport;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogControllerTest extends WebTestClientSupport {

    private static final ParameterizedTypeReference<ApiResponse<CategoryResponse>> CATEGORY_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<PointItemResponse>> ITEM_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<RewardResponse>> REWARD_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiResponse<Void>> VOID_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Test
    void catalogCrudShouldWork() {
        WebTestClient client = withBearerToken(loginAsPlatformAdmin().accessToken());

        ApiResponse<CategoryResponse> categoryCreate = readBody(client.post()
                .uri("/api/catalog/categories")
                .bodyValue(new CategoryCreateRequest(1L, "行为积分", false))
                .exchange(), CATEGORY_TYPE);
        CategoryResponse category = categoryCreate.data();
        assertEquals("行为积分", category.name());

        ApiResponse<CategoryResponse> renamed = readBody(client.put()
                .uri("/api/catalog/categories/{id}", category.id())
                .bodyValue(new CategoryRenameRequest("课堂积分"))
                .exchange(), CATEGORY_TYPE);
        assertEquals("课堂积分", renamed.data().name());

        ApiResponse<PointItemResponse> itemCreate = readBody(client.post()
                .uri("/api/catalog/items")
                .bodyValue(new PointItemCreateRequest(1L, category.id(), "准时到课", 5, true, false))
                .exchange(), ITEM_TYPE);
        PointItemResponse item = itemCreate.data();
        assertEquals(5, item.score());

        ApiResponse<PointItemResponse> itemUpdated = readBody(client.put()
                .uri("/api/catalog/items/{id}", item.id())
                .bodyValue(new PointItemUpdateRequest("迟到扣分", -3, false))
                .exchange(), ITEM_TYPE);
        assertEquals("迟到扣分", itemUpdated.data().name());

        ApiResponse<Void> deleteItem = readBody(client.delete()
                .uri("/api/catalog/items/{id}", item.id())
                .exchange(), VOID_TYPE);
        assertEquals(ApiResponse.SUCCESS_CODE, deleteItem.code());

        ApiResponse<RewardResponse> rewardCreate = readBody(client.post()
                .uri("/api/catalog/rewards")
                .bodyValue(new RewardCreateRequest(1L, "电影票", 100, false))
                .exchange(), REWARD_TYPE);
        RewardResponse reward = rewardCreate.data();
        assertEquals("电影票", reward.name());

        ApiResponse<RewardResponse> rewardUpdated = readBody(client.put()
                .uri("/api/catalog/rewards/{id}", reward.id())
                .bodyValue(new RewardUpdateRequest("乐园票", 150))
                .exchange(), REWARD_TYPE);
        assertEquals(150, rewardUpdated.data().cost());

        ApiResponse<Void> rewardDelete = readBody(client.delete()
                .uri("/api/catalog/rewards/{id}", reward.id())
                .exchange(), VOID_TYPE);
        assertEquals(ApiResponse.SUCCESS_CODE, rewardDelete.code());

        List<CategoryResponse> categories = client.get()
                .uri(uriBuilder -> uriBuilder.path("/api/catalog/categories").queryParam("tenantId", 1).build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CategoryResponse.class)
                .returnResult()
                .getResponseBody();
        assertTrue(categories.stream().anyMatch(c -> c.name().contains("课堂")));
    }
}
