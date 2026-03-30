package com.paravai.communities.membership.application.command.reject;

import com.paravai.communities.membership.domain.model.Membership;

import java.util.Objects;

public final class RejectMembershipResult {

    private final Membership membership;
    private final boolean changed;

    private RejectMembershipResult(Membership membership, boolean changed) {
        this.membership = Objects.requireNonNull(membership, "membership is required");
        this.changed = changed;
    }

    public static RejectMembershipResult changed(Membership membership) {
        return new RejectMembershipResult(membership, true);
    }

    public static RejectMembershipResult unchanged(Membership membership) {
        return new RejectMembershipResult(membership, false);
    }

    public Membership membership() {
        return membership;
    }

    public boolean changed() {
        return changed;
    }
}