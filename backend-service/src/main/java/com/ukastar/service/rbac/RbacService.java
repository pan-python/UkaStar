package com.ukastar.service.rbac;

import com.ukastar.domain.rbac.MenuNode;
import com.ukastar.domain.rbac.Permission;
import com.ukastar.domain.rbac.PermissionType;
import com.ukastar.infra.rbac.InMemoryRbacStore;
import com.ukastar.security.model.CurrentUser;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限画像服务，基于内存元数据返回账号的菜单、按钮与接口权限信息。
 */
@Service
public class RbacService {

    private final InMemoryRbacStore rbacStore;

    public RbacService(InMemoryRbacStore rbacStore) {
        this.rbacStore = rbacStore;
    }

    public Mono<RbacProfile> buildProfile(CurrentUser currentUser) {
        return Mono.fromSupplier(() -> createProfile(currentUser));
    }

    private RbacProfile createProfile(CurrentUser currentUser) {
        Set<String> roleCodes = Set.copyOf(currentUser.roleCodes());
        Set<String> permissionCodes = Set.copyOf(currentUser.permissionCodes());
        Set<Permission> permissions = rbacStore.permissionsByCodes(permissionCodes);
        Map<PermissionType, Set<String>> grouped = permissions.stream()
                .collect(Collectors.groupingBy(
                        Permission::type,
                        () -> new EnumMap<>(PermissionType.class),
                        Collectors.mapping(Permission::code, Collectors.toSet())
                ));
        Set<String> menuPermissions = grouped.getOrDefault(PermissionType.MENU, Set.of());
        Set<String> buttonPermissions = grouped.getOrDefault(PermissionType.BUTTON, Set.of());
        Set<String> apiPermissions = grouped.getOrDefault(PermissionType.API, Set.of());
        List<MenuNode> menus = rbacStore.filterMenuTree(menuPermissions);
        return new RbacProfile(
                currentUser.id(),
                currentUser.tenantId(),
                currentUser.username(),
                currentUser.dataScope(),
                roleCodes,
                Set.copyOf(menuPermissions),
                Set.copyOf(buttonPermissions),
                Set.copyOf(apiPermissions),
                menus
        );
    }
}
