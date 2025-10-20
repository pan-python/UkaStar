package com.ukastar.api.catalog.vo;

import jakarta.validation.constraints.NotBlank;

/**
 * 奖励更新请求。
 */
public record RewardUpdateRequest(
        @NotBlank String name,
        int cost
) {
}
