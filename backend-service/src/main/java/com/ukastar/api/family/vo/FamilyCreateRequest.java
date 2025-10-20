package com.ukastar.api.family.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 家庭创建请求。
 */
public record FamilyCreateRequest(
        @NotNull Long tenantId,
        @NotBlank String familyName
) {
}
