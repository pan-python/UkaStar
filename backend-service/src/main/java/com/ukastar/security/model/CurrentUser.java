package com.ukastar.security.model;

import com.ukastar.domain.rbac.DataScope;

import java.util.Set;

/**
 * 当前登录用户抽象，预留权限与租户信息。
 */
public record CurrentUser(
        Long id,
        Long tenantId,
        String username,
        Set<String> roleCodes,
        Set<String> permissionCodes,
        Set<String> authorities,
        DataScope dataScope
) {

    public boolean hasPermission(String permissionCode) {
        return permissionCodes.contains(permissionCode) || authorities.contains(permissionCode);
    }

    public static CurrentUser system() {
        return new CurrentUser(0L, 0L, "system", Set.of("SYSTEM"), Set.of(), Set.of("ROLE_SYSTEM"), DataScope.TENANT);
    }
}
