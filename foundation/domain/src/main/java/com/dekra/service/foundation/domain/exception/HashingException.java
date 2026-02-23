package com.dekra.service.foundation.domain.exception;

/**
 * Custom exception for hashing related errors.
 */
public class HashingException extends RuntimeException {
    public HashingException(String message, Throwable cause) {
        super(message, cause);
    }
}
