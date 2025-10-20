package com.ukastar.domain.rbac;

import java.util.Objects;
import java.util.Set;

/**
 * 角色定义，包含所属权限与数据范围。
 */
public record Role(
        String code,
        String name,
        DataScope dataScope,
        Set<String> permissionCodes
) {
    public Role {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(dataScope, "dataScope");
        Objects.requireNonNull(permissionCodes, "permissionCodes");
    }
}
