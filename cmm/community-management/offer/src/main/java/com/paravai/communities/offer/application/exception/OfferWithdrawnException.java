package com.paravai.communities.offer.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public class OfferWithdrawnException extends CustomException {

    public OfferWithdrawnException(String messageKey, Object[] args) {
        super(messageKey, HttpStatus.CONFLICT, args);
    }
}