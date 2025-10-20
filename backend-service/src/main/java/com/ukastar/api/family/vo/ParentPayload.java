package com.ukastar.api.family.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 家长绑定信息。
 */
public record ParentPayload(
        Long id,
        @NotNull Long tenantId,
        @NotBlank String name,
        @NotBlank String phoneNumber
) {
}
