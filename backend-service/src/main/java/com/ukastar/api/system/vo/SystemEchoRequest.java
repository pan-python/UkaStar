package com.ukastar.api.system.vo;

import jakarta.validation.constraints.NotBlank;

/**
 * 用于验证参数校验链路的请求体。
 */
public record SystemEchoRequest(@NotBlank(message = "message 不能为空") String message) {
}
