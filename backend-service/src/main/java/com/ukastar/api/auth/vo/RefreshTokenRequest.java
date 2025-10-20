package com.ukastar.api.auth.vo;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新或注销请求体。
 */
public record RefreshTokenRequest(
        @NotBlank(message = "refreshToken 不能为空") String refreshToken
) {
}
