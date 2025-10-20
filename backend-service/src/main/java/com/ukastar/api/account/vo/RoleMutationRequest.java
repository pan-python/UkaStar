package com.ukastar.api.account.vo;

import com.ukastar.domain.rbac.DataScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * 角色创建或更新请求。
 */
public record RoleMutationRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull DataScope dataScope,
        @NotEmpty Set<String> permissionCodes
) {
}
