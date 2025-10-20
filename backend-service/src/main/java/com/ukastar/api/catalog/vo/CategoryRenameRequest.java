package com.ukastar.api.catalog.vo;

import jakarta.validation.constraints.NotBlank;

/**
 * 类别重命名请求。
 */
public record CategoryRenameRequest(
        @NotBlank String name
) {
}
