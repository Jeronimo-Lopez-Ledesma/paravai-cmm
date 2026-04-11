package com.paravai.communities.composition.offer.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public final class CommunityNotFoundException extends CustomException {
    public CommunityNotFoundException(String communityId) {
        super("error.community.notFound", HttpStatus.NOT_FOUND, communityId);
    }
}