package com.paravai.communities.membership.application.command.request;

import com.paravai.communities.membership.domain.model.Membership;

import java.util.Objects;

public record RequestMembershipResult(
        Membership membership,
        boolean created
) {
    public RequestMembershipResult {
        Objects.requireNonNull(membership, "membership is required");
    }

    public static RequestMembershipResult created(Membership membership) {
        return new RequestMembershipResult(membership, true);
    }

    public static RequestMembershipResult existing(Membership membership) {
        return new RequestMembershipResult(membership, false);
    }
}