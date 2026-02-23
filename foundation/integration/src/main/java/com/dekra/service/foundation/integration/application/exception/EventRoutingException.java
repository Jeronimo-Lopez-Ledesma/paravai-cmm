package com.dekra.service.foundation.integration.application.exception;

public class EventRoutingException extends RuntimeException {

    public EventRoutingException(String message) {
        super(message);
    }

    public EventRoutingException(String message, Throwable cause) {
        super(message, cause);
    }
}
