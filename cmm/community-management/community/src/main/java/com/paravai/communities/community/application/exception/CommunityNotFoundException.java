package com.paravai.communities.community.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import com.paravai.foundation.domain.value.IdValue;
import org.springframework.http.HttpStatus;

import java.util.Objects;

public final class CommunityNotFoundException extends CustomException {

    private final IdValue communityId;

    public CommunityNotFoundException(IdValue communityId) {
        super(
                "community.error.notFound",      // message key
                HttpStatus.NOT_FOUND,            // status
                communityId != null ? communityId.value() : null
        );
        this.communityId = Objects.requireNonNull(communityId, "communityId");
    }

    public IdValue communityId() {
        return communityId;
    }
}