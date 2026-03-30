package com.paravai.communities.membership.application.query.getmy;

import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.value.CommunityRoleValue;

import java.util.Objects;
import java.util.Optional;

public final class GetMyMembershipResult {

    public enum Status {
        NONE,
        PENDING,
        ACTIVE,
        REJECTED
    }

    private final Status status;
    private final CommunityRoleValue role; // only for ACTIVE

    private GetMyMembershipResult(Status status, CommunityRoleValue role) {
        this.status = Objects.requireNonNull(status);
        this.role = role;
    }

    public static GetMyMembershipResult none() {
        return new GetMyMembershipResult(Status.NONE, null);
    }

    public static GetMyMembershipResult fromMembership(Membership m) {
        if (m.isActive()) {
            return new GetMyMembershipResult(
                    Status.ACTIVE,
                    m.role().orElse(null)
            );
        }
        if (m.isPending()) {
            return new GetMyMembershipResult(Status.PENDING, null);
        }
        if (m.isRejected()) {
            return new GetMyMembershipResult(Status.REJECTED, null);
        }

        throw new IllegalStateException("Unknown membership state");
    }

    public Status status() {
        return status;
    }

    public Optional<CommunityRoleValue> role() {
        return Optional.ofNullable(role);
    }
}