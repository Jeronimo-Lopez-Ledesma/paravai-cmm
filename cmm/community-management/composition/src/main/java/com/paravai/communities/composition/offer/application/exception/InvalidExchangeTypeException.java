package com.paravai.communities.composition.offer.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public final class InvalidExchangeTypeException extends CustomException {
    public InvalidExchangeTypeException(String exchangeType) {
        super("error.offer.invalidExchangeType", HttpStatus.BAD_REQUEST, exchangeType);
    }
}