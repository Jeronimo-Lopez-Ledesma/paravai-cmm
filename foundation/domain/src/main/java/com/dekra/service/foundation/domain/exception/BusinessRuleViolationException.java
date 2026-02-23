package com.paravai.foundation.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BusinessRuleViolationException extends ResponseStatusException {
    public BusinessRuleViolationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}