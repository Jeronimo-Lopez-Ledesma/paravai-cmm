package com.paravai.communities.composition.offer.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public final class ExchangeTypeNotAllowedException extends CustomException {
    public ExchangeTypeNotAllowedException(String exchangeType, String communityId) {
        super("error.offer.exchangeTypeNotAllowed", HttpStatus.CONFLICT, exchangeType, communityId);
    }
}