package com.paravai.communities.composition.offer.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public final class UnauthenticatedUserException extends CustomException {
    public UnauthenticatedUserException() {
        super("error.auth.unauthenticated", HttpStatus.UNAUTHORIZED);
    }
}