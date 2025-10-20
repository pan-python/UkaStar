package com.ukastar.api.account.vo;

import com.ukastar.domain.rbac.DataScope;

import java.util.Set;

/**
 * 账号响应。
 */
public record AccountResponse(
        Long id,
        Long tenantId,
        String username,
        boolean active,
        Set<String> roleCodes,
        Set<String> permissionCodes,
        DataScope dataScope
) {
}
