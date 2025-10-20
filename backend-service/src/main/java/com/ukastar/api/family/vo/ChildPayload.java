package com.ukastar.api.family.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * 孩子绑定信息。
 */
public record ChildPayload(
        Long id,
        @NotNull Long tenantId,
        @NotBlank String name,
        Instant birthday
) {
}
