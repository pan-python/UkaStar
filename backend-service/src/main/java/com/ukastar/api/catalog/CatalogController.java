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
import com.ukastar.service.catalog.CatalogService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 积分类别/项目/奖励接口。
 */
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
    public Flux<CategoryResponse> listCategories(@RequestParam Long tenantId) {
        return catalogService.listCategories(tenantId)
                .map(category -> new CategoryResponse(category.id(), category.tenantId(), category.name(), category.systemDefault(), category.createdAt(), category.updatedAt()));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
    public Mono<ApiResponse<CategoryResponse>> createCategory(@RequestBody Mono<@Valid CategoryCreateRequest> requestMono) {
        return requestMono.flatMap(request -> catalogService.createCategory(request.tenantId(), request.name(), request.systemDefault()))
                .map(category -> new CategoryResponse(category.id(), category.tenantId(), category.name(), category.systemDefault(), category.createdAt(), category.updatedAt()))
                .map(ApiResponse::success);
    }

    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
    public Mono<ApiResponse<CategoryResponse>> renameCategory(@PathVariable Long categoryId, @RequestBody Mono<@Valid CategoryRenameRequest> requestMono) {
        return requestMono.flatMap(request -> catalogService.renameCategory(categoryId, request.name()))
                .map(category -> new CategoryResponse(category.id(), category.tenantId(), category.name(), category.systemDefault(), category.createdAt(), category.updatedAt()))
                .map(ApiResponse::success);
    }

    @GetMapping("/items")
    @PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
    public Flux<PointItemResponse> listItems(@RequestParam Long tenantId) {
        return catalogService.listItems(tenantId)
                .map(item -> new PointItemResponse(item.id(), item.tenantId(), item.categoryId(), item.name(), item.score(), item.positive(), item.systemDefault(), item.createdAt(), item.updatedAt()));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
    public Mono<ApiResponse<PointItemResponse>> createItem(@RequestBody Mono<@Valid PointItemCreateRequest> requestMono) {
        return requestMono.flatMap(request -> catalogService.createItem(request.tenantId(), request.categoryId(), request.name(), request.score(), request.positive(), request.systemDefault()))
                .map(item -> new PointItemResponse(item.id(), item.tenantId(), item.categoryId(), item.name(), item.score(), item.positive(), item.systemDefault(), item.createdAt(), item.updatedAt()))
                .map(ApiResponse::success);
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
    public Mono<ApiResponse<PointItemResponse>> updateItem(@PathVariable Long itemId, @RequestBody Mono<@Valid PointItemUpdateRequest> requestMono) {
        return requestMono.flatMap(request -> catalogService.updateItem(itemId, request.name(), request.score(), request.positive()))
                .map(item -> new PointItemResponse(item.id(), item.tenantId(), item.categoryId(), item.name(), item.score(), item.positive(), item.systemDefault(), item.createdAt(), item.updatedAt()))
                .map(ApiResponse::success);
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
    public Mono<ApiResponse<Void>> deleteItem(@PathVariable Long itemId) {
        return catalogService.deleteItem(itemId).thenReturn(ApiResponse.success());
    }

    @GetMapping("/rewards")
    @PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
    public Flux<RewardResponse> listRewards(@RequestParam Long tenantId) {
        return catalogService.listRewards(tenantId)
                .map(reward -> new RewardResponse(reward.id(), reward.tenantId(), reward.name(), reward.cost(), reward.systemDefault(), reward.createdAt(), reward.updatedAt()));
    }

    @PostMapping("/rewards")
    @PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
    public Mono<ApiResponse<RewardResponse>> createReward(@RequestBody Mono<@Valid RewardCreateRequest> requestMono) {
        return requestMono.flatMap(request -> catalogService.createReward(request.tenantId(), request.name(), request.cost(), request.systemDefault()))
                .map(reward -> new RewardResponse(reward.id(), reward.tenantId(), reward.name(), reward.cost(), reward.systemDefault(), reward.createdAt(), reward.updatedAt()))
                .map(ApiResponse::success);
    }

    @PutMapping("/rewards/{rewardId}")
    @PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
    public Mono<ApiResponse<RewardResponse>> updateReward(@PathVariable Long rewardId, @RequestBody Mono<@Valid RewardUpdateRequest> requestMono) {
        return requestMono.flatMap(request -> catalogService.updateReward(rewardId, request.name(), request.cost()))
                .map(reward -> new RewardResponse(reward.id(), reward.tenantId(), reward.name(), reward.cost(), reward.systemDefault(), reward.createdAt(), reward.updatedAt()))
                .map(ApiResponse::success);
    }

    @DeleteMapping("/rewards/{rewardId}")
    @PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
    public Mono<ApiResponse<Void>> deleteReward(@PathVariable Long rewardId) {
        return catalogService.deleteReward(rewardId).thenReturn(ApiResponse.success());
    }
}
