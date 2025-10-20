package com.ukastar.api.account.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * 账号创建请求。
 */
public record AccountCreateRequest(
        @NotNull Long tenantId,
        @NotBlank String username,
        @NotBlank String password,
        @NotEmpty Set<String> roleCodes
) {
}
