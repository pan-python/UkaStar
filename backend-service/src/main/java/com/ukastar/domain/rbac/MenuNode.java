package com.ukastar.domain.rbac;

import java.util.List;
import java.util.Objects;

/**
 * 菜单节点定义。
 */
public record MenuNode(
        String code,
        String name,
        String path,
        String icon,
        String permissionCode,
        List<MenuNode> children
) {
    public MenuNode {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(permissionCode, "permissionCode");
        children = children == null ? List.of() : List.copyOf(children);
    }
}
