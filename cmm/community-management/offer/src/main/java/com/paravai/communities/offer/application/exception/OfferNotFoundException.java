package com.paravai.communities.offer.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OfferNotFoundException extends CustomException {

    public OfferNotFoundException(String messageKey, Object[] args) {
        super(messageKey, HttpStatus.NOT_FOUND, args);
    }
}