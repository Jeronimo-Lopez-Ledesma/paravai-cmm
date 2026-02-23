package com.dekra.service.foundation.domaincore.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends CustomException {

    public ServiceUnavailableException(String messageKey, Throwable cause, Object... args) {
        super(messageKey, HttpStatus.SERVICE_UNAVAILABLE, args);
        initCause(cause);
    }

    public ServiceUnavailableException(String messageKey, Object... args) {
        super(messageKey, HttpStatus.SERVICE_UNAVAILABLE, args);
    }
}

