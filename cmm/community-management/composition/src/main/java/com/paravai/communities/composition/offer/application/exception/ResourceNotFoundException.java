package com.paravai.communities.composition.offer.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public final class ResourceNotFoundException extends CustomException {
    public ResourceNotFoundException(String resourceId) {
        super("error.resource.notFound", HttpStatus.NOT_FOUND, resourceId);
    }
}