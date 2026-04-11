package com.paravai.communities.composition.offer.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public final class DuplicateActiveOfferException extends CustomException {


    public DuplicateActiveOfferException() {
        super("error.offer.duplicateActiveOffer", HttpStatus.CONFLICT);
    }

    public DuplicateActiveOfferException(String resourceId, String communityId) {
        super("error.offer.duplicateActiveOffer", HttpStatus.CONFLICT, resourceId, communityId);
    }
}