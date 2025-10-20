package com.ukastar.api.rbac;

import com.ukastar.api.rbac.vo.MenuNodeResponse;
import com.ukastar.api.rbac.vo.RbacProfileResponse;
import com.ukastar.common.response.ApiResponse;
import com.ukastar.domain.rbac.MenuNode;
import com.ukastar.security.support.CurrentUserProvider;
import com.ukastar.service.rbac.RbacProfile;
import com.ukastar.service.rbac.RbacService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * RBAC 相关接口，提供当前账号的权限画像。
 */
@RestController
@RequestMapping("/api/rbac")
public class RbacController {

    private final RbacService rbacService;
    private final CurrentUserProvider currentUserProvider;

    public RbacController(RbacService rbacService, CurrentUserProvider currentUserProvider) {
        this.rbacService = rbacService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('PERM_RBAC_PROFILE_VIEW')")
    public Mono<ApiResponse<RbacProfileResponse>> profile() {
        return currentUserProvider.currentUser()
                .flatMap(rbacService::buildProfile)
                .map(this::toResponse)
                .map(ApiResponse::success);
    }

    private RbacProfileResponse toResponse(RbacProfile profile) {
        List<MenuNodeResponse> menus = profile.menus().stream()
                .map(this::toResponse)
                .toList();
        return new RbacProfileResponse(
                profile.accountId(),
                profile.tenantId(),
                profile.username(),
                profile.dataScope(),
                profile.roles(),
                profile.menuPermissions(),
                profile.buttonPermissions(),
                profile.apiPermissions(),
                menus
        );
    }

    private MenuNodeResponse toResponse(MenuNode node) {
        List<MenuNodeResponse> children = node.children().stream()
                .map(this::toResponse)
                .toList();
        return new MenuNodeResponse(node.code(), node.name(), node.path(), node.icon(), children);
    }
}
