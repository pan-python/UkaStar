package com.ukastar.api.tenant.vo;

import jakarta.validation.constraints.NotBlank;

/**
 * 租户更新请求。
 */
public record TenantUpdateRequest(
        @NotBlank String name,
        @NotBlank String contactName,
        @NotBlank String contactPhone
) {
}
