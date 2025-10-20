package com.ukastar.common.error;

/**
 * 系统标准错误码定义。
 */
public enum ErrorCode {
    VALIDATION_ERROR("VALIDATION_ERROR", "参数校验失败"),
    BUSINESS_ERROR("BUSINESS_ERROR", "业务异常"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "资源不存在"),
    UNAUTHORIZED("UNAUTHORIZED", "未认证或凭证已失效"),
    FORBIDDEN("FORBIDDEN", "无权限访问"),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统内部错误");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
