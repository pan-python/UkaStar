package com.ukastar.common.response;

import com.ukastar.common.trace.TraceIdHolder;

import java.time.Instant;
import java.util.Objects;

/**
 * 标准 API 返回体，统一携带响应码、消息、数据、时间戳与 TraceId。
 */
public record ApiResponse<T>(String code, String message, T data, Instant timestamp, String traceId) {

    public static final String SUCCESS_CODE = "SUCCESS";

    public ApiResponse {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    public static <T> ApiResponse<T> success(T data) {
        return build(SUCCESS_CODE, "OK", data);
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static ApiResponse<Void> failure(String code, String message) {
        return build(code, message, null);
    }

    private static <T> ApiResponse<T> build(String code, String message, T data) {
        return new ApiResponse<>(code, message, data, Instant.now(), TraceIdHolder.get());
    }
}
