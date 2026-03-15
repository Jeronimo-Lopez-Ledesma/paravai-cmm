package com.paravai.communities.membership.application.command.createfounder;

import com.paravai.foundation.domain.value.IdValue;

import java.util.Objects;

public record CreateFounderMembershipRequest(
        IdValue tenantId,
        IdValue communityId,
        IdValue founderUserId,
        String traceId,
        String sourceSystem
) {
    public CreateFounderMembershipRequest {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(founderUserId, "founderUserId is required");
        Objects.requireNonNull(sourceSystem, "sourceSystem is required");
    }
}