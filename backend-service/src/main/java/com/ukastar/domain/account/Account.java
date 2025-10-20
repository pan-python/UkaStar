package com.ukastar.domain.account;

import com.ukastar.domain.rbac.DataScope;

import java.util.Set;

/**
 * 账号领域模型，包含租户、权限、角色以及令牌版本等信息。
 */
public record Account(
        Long id,
        Long tenantId,
        String username,
        String passwordHash,
        Set<String> roleCodes,
        Set<String> permissionCodes,
        Set<String> authorities,
        DataScope dataScope,
        boolean active,
        long tokenVersion
) {

    public Account withTokenVersion(long newVersion) {
        return new Account(id, tenantId, username, passwordHash, roleCodes, permissionCodes, authorities, dataScope, active, newVersion);
    }
}
