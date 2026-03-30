package com.paravai.communities.membership.application.command.assignrole;

import com.paravai.communities.membership.domain.model.Membership;

import java.util.Objects;

public final class AssignCommunityRoleResult {

    private final Membership membership;
    private final boolean changed;

    private AssignCommunityRoleResult(Membership membership, boolean changed) {
        this.membership = Objects.requireNonNull(membership, "membership is required");
        this.changed = changed;
    }

    public static AssignCommunityRoleResult changed(Membership membership) {
        return new AssignCommunityRoleResult(membership, true);
    }

    public static AssignCommunityRoleResult unchanged(Membership membership) {
        return new AssignCommunityRoleResult(membership, false);
    }

    public Membership membership() {
        return membership;
    }

    public boolean changed() {
        return changed;
    }
}