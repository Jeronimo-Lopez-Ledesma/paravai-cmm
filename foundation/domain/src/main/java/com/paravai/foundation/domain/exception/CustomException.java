package com.paravai.foundation.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException{
    // Error types
    public static final String USER_NOT_FOUND = "error.user_not_found";
    public static final String REQUEST_NOT_FOUND = "error.request_not_found";
    public static final String MODULES_EXCEPTION = "error.modules_exception";
    public static final String USER_OID_NOT_NULL = "{error.user_oid_not_null}";
    public static final String REQUEST_MODULES_NOT_NULL = "{error.request_modules_not_null}";
    public static final String UNAUTHORIZED_CONNECTION = "error.unauthorized_connection";
    public static final String REDIS_UNAVAILABLE = "error.redis_unavailable";

    private final String messageKey;
    private final Object[] args;
    private final HttpStatus codeStatus;

    public CustomException(String messageKey, HttpStatus codeStatus, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
        this.codeStatus = codeStatus;
    }

}
