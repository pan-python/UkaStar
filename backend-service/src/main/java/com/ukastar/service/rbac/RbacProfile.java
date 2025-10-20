package com.ukastar.service.rbac;

import com.ukastar.domain.rbac.DataScope;
import com.ukastar.domain.rbac.MenuNode;

import java.util.List;
import java.util.Set;

/**
 * 封装当前账号的权限画像。
 */
public record RbacProfile(
        Long accountId,
        Long tenantId,
        String username,
        DataScope dataScope,
        Set<String> roles,
        Set<String> menuPermissions,
        Set<String> buttonPermissions,
        Set<String> apiPermissions,
        List<MenuNode> menus
) {
}
