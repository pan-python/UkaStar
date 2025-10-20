package com.ukastar.common.exception;

import com.ukastar.common.error.ErrorCode;

/**
 * 业务异常，携带标准错误码。
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage());
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
