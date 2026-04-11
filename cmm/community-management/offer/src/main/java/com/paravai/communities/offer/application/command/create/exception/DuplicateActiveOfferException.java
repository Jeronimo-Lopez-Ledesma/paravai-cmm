package com.paravai.communities.offer.application.command.create.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DuplicateActiveOfferException extends CustomException {

    public DuplicateActiveOfferException(String resourceId, String communityId) {
        super("error.offer.duplicateActiveOffer", HttpStatus.CONFLICT, resourceId, communityId);
    }
}