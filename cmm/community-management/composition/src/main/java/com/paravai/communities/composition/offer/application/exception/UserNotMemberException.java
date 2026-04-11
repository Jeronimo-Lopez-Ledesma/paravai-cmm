package com.paravai.communities.composition.offer.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import org.springframework.http.HttpStatus;

public class UserNotMemberException extends CustomException {

    public UserNotMemberException(String userId, String communityId) {
        super("error.membership.notMember", HttpStatus.FORBIDDEN, userId, communityId);
    }
}