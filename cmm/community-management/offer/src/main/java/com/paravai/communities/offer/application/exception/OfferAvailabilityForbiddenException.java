package com.paravai.communities.offer.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OfferAvailabilityForbiddenException extends CustomException {

    public OfferAvailabilityForbiddenException(String messageKey, Object[] args) {
        super(messageKey, HttpStatus.FORBIDDEN, args);
    }
}