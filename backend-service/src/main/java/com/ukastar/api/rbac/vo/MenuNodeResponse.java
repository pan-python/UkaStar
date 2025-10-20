package com.ukastar.api.rbac.vo;

import java.util.List;

/**
 * RBAC 菜单节点响应。
 */
public record MenuNodeResponse(
        String code,
        String name,
        String path,
        String icon,
        List<MenuNodeResponse> children
) {
}
