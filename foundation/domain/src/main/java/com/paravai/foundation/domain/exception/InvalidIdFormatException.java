package com.paravai.foundation.domain.exception;

public class InvalidIdFormatException extends RuntimeException {
    public InvalidIdFormatException(String value) {
        super("Invalid UUID format for ID: " + value);
    }
}
