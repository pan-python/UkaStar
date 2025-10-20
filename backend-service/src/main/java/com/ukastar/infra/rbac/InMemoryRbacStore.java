package com.ukastar.infra.rbac;

import com.ukastar.domain.rbac.DataScope;
import com.ukastar.domain.rbac.MenuNode;
import com.ukastar.domain.rbac.Permission;
import com.ukastar.domain.rbac.PermissionType;
import com.ukastar.domain.rbac.Role;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RBAC 元数据的内存实现，用于在引入数据库前提供完整的权限体验。
 */
@Component
public class InMemoryRbacStore {

    private final Map<String, Permission> permissionsByCode;
    private final Map<String, Role> rolesByCode;
    private final List<MenuNode> menuTree;

    public InMemoryRbacStore() {
        this.permissionsByCode = new HashMap<>();
        this.rolesByCode = new HashMap<>();
        this.menuTree = buildMenuTree();
        registerPermissions();
        registerRoles();
    }

    public synchronized List<Role> listRoles() {
        return List.copyOf(rolesByCode.values());
    }

    public synchronized List<Permission> listPermissions() {
        return List.copyOf(permissionsByCode.values());
    }

    public Set<Permission> permissionsByCodes(Set<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Set.of();
        }
        return codes.stream()
                .map(permissionsByCode::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<Role> rolesByCodes(Set<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Set.of();
        }
        return codes.stream()
                .map(rolesByCode::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> permissionCodesForRoles(Set<String> roleCodes) {
        return rolesByCodes(roleCodes).stream()
                .flatMap(role -> role.permissionCodes().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    public DataScope resolveDataScope(Set<String> roleCodes, Set<String> permissionCodes) {
        DataScope scope = DataScope.SELF;
        for (Role role : rolesByCodes(roleCodes)) {
            scope = DataScope.max(scope, role.dataScope());
        }
        for (Permission permission : permissionsByCodes(permissionCodes)) {
            scope = DataScope.max(scope, permission.dataScope());
        }
        return scope;
    }

    public Set<String> authoritiesFor(Set<String> roleCodes, Set<String> permissionCodes) {
        Set<String> authorities = new HashSet<>();
        for (String role : roleCodes) {
            authorities.add("ROLE_" + role);
        }
        if (permissionCodes != null) {
            authorities.addAll(permissionCodes);
        }
        return Set.copyOf(authorities);
    }

    public List<MenuNode> filterMenuTree(Set<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return List.of();
        }
        List<MenuNode> filtered = new ArrayList<>();
        for (MenuNode node : menuTree) {
            MenuNode permitted = filterNode(node, permissionCodes);
            if (permitted != null) {
                filtered.add(permitted);
            }
        }
        return List.copyOf(filtered);
    }

    private MenuNode filterNode(MenuNode node, Set<String> permissionCodes) {
        List<MenuNode> children = node.children().stream()
                .map(child -> filterNode(child, permissionCodes))
                .filter(Objects::nonNull)
                .toList();
        boolean hasAccess = permissionCodes.contains(node.permissionCode()) || !children.isEmpty();
        if (!hasAccess) {
            return null;
        }
        return new MenuNode(node.code(), node.name(), node.path(), node.icon(), node.permissionCode(), children);
    }

    private void registerPermissions() {
        register(new Permission("MENU_DASHBOARD_OVERVIEW", "仪表盘", PermissionType.MENU, DataScope.TENANT, "仪表盘菜单"));
        register(new Permission("MENU_SYSTEM_CENTER", "系统管理", PermissionType.MENU, DataScope.TENANT, "系统管理菜单"));
        register(new Permission("MENU_SYSTEM_INFO", "系统信息", PermissionType.MENU, DataScope.TENANT, "系统信息菜单"));
        register(new Permission("MENU_SYSTEM_RBAC", "权限配置", PermissionType.MENU, DataScope.TENANT, "权限配置菜单"));
        register(new Permission("MENU_POINTS_CENTER", "积分中心", PermissionType.MENU, DataScope.TENANT, "积分管理菜单"));
        register(new Permission("MENU_AUDIT_CENTER", "审计日志", PermissionType.MENU, DataScope.TENANT, "审计菜单"));

        register(new Permission("BUTTON_SYSTEM_INFO_REFRESH", "刷新系统信息", PermissionType.BUTTON, DataScope.GROUP, "系统信息页刷新按钮"));

        register(new Permission("PERM_SYSTEM_INFO_VIEW", "查看系统信息", PermissionType.API, DataScope.TENANT, "查询系统基础信息"));
        register(new Permission("PERM_SYSTEM_INFO_ECHO", "系统回声", PermissionType.API, DataScope.GROUP, "回声测试接口"));
        register(new Permission("PERM_RBAC_PROFILE_VIEW", "查看权限配置", PermissionType.API, DataScope.TENANT, "查看当前账号权限配置"));
        register(new Permission("PERM_TENANT_MANAGE", "租户管理", PermissionType.API, DataScope.GROUP, "租户 CRUD 接口"));
        register(new Permission("PERM_ACCOUNT_MANAGE", "账号管理", PermissionType.API, DataScope.TENANT, "账号与角色管理接口"));
        register(new Permission("PERM_FAMILY_MANAGE", "家庭管理", PermissionType.API, DataScope.TENANT, "家庭/家长/孩子接口"));
        register(new Permission("PERM_CATALOG_MANAGE", "积分目录管理", PermissionType.API, DataScope.TENANT, "类别/项目/奖励接口"));
        register(new Permission("PERM_POINTS_OPERATE", "积分操作", PermissionType.API, DataScope.TENANT, "加减积分接口"));
        register(new Permission("PERM_POINTS_VIEW", "积分流水查看", PermissionType.API, DataScope.TENANT, "积分流水与统计查询"));
        register(new Permission("PERM_EXPORT_EXECUTE", "导出执行", PermissionType.API, DataScope.TENANT, "导出任务执行"));
        register(new Permission("PERM_AUDIT_VIEW", "审计查看", PermissionType.API, DataScope.TENANT, "审计日志接口"));
    }

    private void registerRoles() {
        register(new Role(
                "PLATFORM_ADMIN",
                "平台超级管理员",
                DataScope.TENANT,
                Set.copyOf(permissionsByCode.keySet())
        ));

        register(new Role(
                "TENANT_OPERATOR",
                "租户运营",
                DataScope.GROUP,
                Set.of(
                        "MENU_DASHBOARD_OVERVIEW",
                        "MENU_SYSTEM_CENTER",
                        "MENU_SYSTEM_INFO",
                        "MENU_POINTS_CENTER",
                        "MENU_AUDIT_CENTER",
                        "BUTTON_SYSTEM_INFO_REFRESH",
                        "PERM_SYSTEM_INFO_VIEW",
                        "PERM_SYSTEM_INFO_ECHO",
                        "PERM_RBAC_PROFILE_VIEW",
                        "PERM_ACCOUNT_MANAGE",
                        "PERM_FAMILY_MANAGE",
                        "PERM_CATALOG_MANAGE",
                        "PERM_POINTS_OPERATE",
                        "PERM_POINTS_VIEW",
                        "PERM_EXPORT_EXECUTE",
                        "PERM_AUDIT_VIEW"
                )
        ));

        register(new Role(
                "TENANT_VIEWER",
                "租户只读",
                DataScope.SELF,
                Set.of(
                        "MENU_DASHBOARD_OVERVIEW",
                        "MENU_SYSTEM_CENTER",
                        "MENU_SYSTEM_INFO",
                        "MENU_POINTS_CENTER",
                        "MENU_AUDIT_CENTER",
                        "PERM_RBAC_PROFILE_VIEW",
                        "PERM_SYSTEM_INFO_VIEW",
                        "PERM_POINTS_VIEW",
                        "PERM_AUDIT_VIEW"
                )
        ));
    }

    private void register(Permission permission) {
        permissionsByCode.put(permission.code(), permission);
    }

    private void register(Role role) {
        rolesByCode.put(role.code(), role);
    }

    public synchronized Role createRole(String code, String name, DataScope scope, Set<String> permissionCodes) {
        Role role = new Role(code, name, scope, Set.copyOf(permissionCodes));
        rolesByCode.put(code, role);
        return role;
    }

    public synchronized Role updateRole(String code, String name, DataScope scope, Set<String> permissionCodes) {
        DataScope resolvedScope = scope;
        if (resolvedScope == null) {
            Role existing = rolesByCode.get(code);
            resolvedScope = existing != null ? existing.dataScope() : DataScope.SELF;
        }
        Role updated = new Role(code, name, resolvedScope, Set.copyOf(permissionCodes));
        rolesByCode.put(code, updated);
        return updated;
    }

    private List<MenuNode> buildMenuTree() {
        MenuNode dashboard = new MenuNode(
                "dashboard",
                "仪表盘",
                "/dashboard",
                "odometer",
                "MENU_DASHBOARD_OVERVIEW",
                List.of()
        );
        MenuNode systemInfo = new MenuNode(
                "system-info",
                "系统信息",
                "/system/info",
                "cpu",
                "MENU_SYSTEM_INFO",
                List.of()
        );
        MenuNode rbacCenter = new MenuNode(
                "rbac-center",
                "权限配置",
                "/system/rbac",
                "lock",
                "MENU_SYSTEM_RBAC",
                List.of()
        );
        MenuNode systemRoot = new MenuNode(
                "system",
                "系统管理",
                "/system",
                "setting",
                "MENU_SYSTEM_CENTER",
                List.of(systemInfo, rbacCenter)
        );
        MenuNode pointsCenter = new MenuNode(
                "points",
                "积分中心",
                "/points",
                "medal",
                "MENU_POINTS_CENTER",
                List.of()
        );
        MenuNode auditCenter = new MenuNode(
                "audit",
                "审计日志",
                "/audit",
                "document",
                "MENU_AUDIT_CENTER",
                List.of()
        );
        return List.of(dashboard, systemRoot, pointsCenter, auditCenter);
    }
}
