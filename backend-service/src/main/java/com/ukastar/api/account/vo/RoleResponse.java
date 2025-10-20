package com.ukastar.api.account.vo;

import com.ukastar.domain.rbac.DataScope;

import java.util.Set;

/**
 * 角色响应。
 */
public record RoleResponse(
        String code,
        String name,
        DataScope dataScope,
        Set<String> permissionCodes
) {
}
