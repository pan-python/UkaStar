package com.ukastar.api.family.vo;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 家长绑定请求。
 */
public record ParentBindingRequest(
        @NotEmpty List<ParentPayload> parents
) {
}
