package com.ukastar.api.family.vo;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 孩子绑定请求。
 */
public record ChildBindingRequest(
        @NotEmpty List<ChildPayload> children
) {
}
