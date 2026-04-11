package com.paravai.communities.composition.offer.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public final class UserNotActiveException extends CustomException {
    public UserNotActiveException(String userOid, String communityId) {
        super("error.membership.notActive", HttpStatus.FORBIDDEN, userOid, communityId);
    }
}