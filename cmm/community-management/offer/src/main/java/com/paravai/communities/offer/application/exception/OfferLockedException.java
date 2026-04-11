package com.paravai.communities.offer.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OfferLockedException extends CustomException {

    public OfferLockedException(String messageKey, Object[] args) {
        super(messageKey, HttpStatus.CONFLICT, args);
    }
}