package com.paravai.communities.community.application.exception;

import com.paravai.foundation.domain.exception.CustomException;
import com.paravai.foundation.domain.value.IdValue;
import org.springframework.http.HttpStatus;

import java.util.Objects;

public final class ForbiddenCommunityOperationException extends CustomException {

    private final IdValue communityId;
    private final IdValue userId;
    private final String operation;

    public ForbiddenCommunityOperationException(IdValue communityId, IdValue userId, String operation) {
        super(
                "community.error.forbiddenOperation",
                HttpStatus.FORBIDDEN,
                operation,
                userId != null ? userId.value() : null,
                communityId != null ? communityId.value() : null
        );
        this.communityId = Objects.requireNonNull(communityId, "communityId");
        this.userId = Objects.requireNonNull(userId, "userId");
        this.operation = Objects.requireNonNull(operation, "operation");
    }

    public IdValue communityId() { return communityId; }
    public IdValue userId() { return userId; }
    public String operation() { return operation; }
}