package com.ukastar.api.catalog.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 奖励创建请求。
 */
public record RewardCreateRequest(
        @NotNull Long tenantId,
        @NotBlank String name,
        int cost,
        boolean systemDefault
) {
}
