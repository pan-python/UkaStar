package com.ukastar.domain.rbac;

import java.util.Objects;

/**
 * 权限定义。
 */
public record Permission(
        String code,
        String name,
        PermissionType type,
        DataScope dataScope,
        String description
) {
    public Permission {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }
}
