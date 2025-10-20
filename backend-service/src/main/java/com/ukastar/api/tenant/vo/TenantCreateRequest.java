package com.ukastar.api.tenant.vo;

import jakarta.validation.constraints.NotBlank;

/**
 * 租户创建请求。
 */
public record TenantCreateRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String contactName,
        @NotBlank String contactPhone
) {
}
