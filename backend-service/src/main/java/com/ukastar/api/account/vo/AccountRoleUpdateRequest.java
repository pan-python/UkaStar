package com.ukastar.api.account.vo;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

/**
 * 账号角色更新请求。
 */
public record AccountRoleUpdateRequest(
        @NotEmpty Set<String> roleCodes
) {
}
