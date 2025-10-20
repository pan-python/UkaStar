package com.ukastar.api.auth.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 登录请求体。
 */
public record LoginRequest(
        @NotNull(message = "tenantId 不能为空") Long tenantId,
        @NotBlank(message = "username 不能为空") String username,
        @NotBlank(message = "password 不能为空") String password
) {
}
