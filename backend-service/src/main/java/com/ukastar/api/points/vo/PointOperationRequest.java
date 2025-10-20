package com.ukastar.api.points.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 积分操作请求。
 */
public record PointOperationRequest(
        @NotNull Long familyId,
        @NotNull Long operatorAccountId,
        @Min(1) int amount,
        @NotBlank String reason
) {
}
