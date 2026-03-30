package com.paravai.communities.membership.application.command.approve;

import com.paravai.communities.membership.domain.model.Membership;

import java.util.Objects;

public record ApproveMembershipResult(
        Membership membership,
        boolean changed
) {
    public ApproveMembershipResult {
        Objects.requireNonNull(membership, "membership is required");
    }

    public static ApproveMembershipResult changed(Membership membership) {
        return new ApproveMembershipResult(membership, true);
    }

    public static ApproveMembershipResult unchanged(Membership membership) {
        return new ApproveMembershipResult(membership, false);
    }
}