package com.ukastar.api.rbac.vo;

import com.ukastar.domain.rbac.DataScope;

import java.util.List;
import java.util.Set;

/**
 * RBAC 权限画像响应体。
 */
public record RbacProfileResponse(
        Long accountId,
        Long tenantId,
        String username,
        DataScope dataScope,
        Set<String> roles,
        Set<String> menuPermissions,
        Set<String> buttonPermissions,
        Set<String> apiPermissions,
        List<MenuNodeResponse> menus
) {
}
