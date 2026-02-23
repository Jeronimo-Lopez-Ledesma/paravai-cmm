package com.paravai.communities.membership.domain.model;

import com.paravai.foundation.domain.value.IdValue;

import java.time.Instant;
import java.util.Objects;

/**
 * Factory: MembershipFactory
 *
 * Encapsulates valid Membership creation and reconstruction.
 *
 * Methods:
 * - create(): for new memberships
 * - recreate(): for rehydration from persistence
 */
public final class MembershipFactory {

    private MembershipFactory() {
        throw new IllegalStateException("Factory class — not instantiable");
    }

    // -------------------------------------------------
    // Creation
    // -------------------------------------------------

    /**
     * Creates the initial ADMIN membership for the community creator (A1 AC2).
     */
    public static Membership createAdmin(IdValue tenantId,
                                         IdValue communityId,
                                         IdValue userId) {

        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(userId, "userId is required");

        Instant now = Instant.now();

        return new Membership(
                IdValue.generate(),
                tenantId,
                communityId,
                userId,
                CommunityRole.ADMIN,
                MembershipStatus.ACTIVE,
                now,
                null,
                now,
                now,
                true
        );
    }

    /**
     * Creates a PENDING invitation (A4).
     * For MVP: invitation is represented as Membership with status=PENDING.
     */
    public static Membership createInvitation(IdValue tenantId,
                                              IdValue communityId,
                                              IdValue inviteeUserId) {

        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(inviteeUserId, "inviteeUserId is required");

        Instant now = Instant.now();

        return new Membership(
                IdValue.generate(),
                tenantId,
                communityId,
                inviteeUserId,
                CommunityRole.MEMBER,
                MembershipStatus.PENDING,
                now,
                null,
                now,
                now,
                true
        );
    }

    // -------------------------------------------------
    // Reconstruction (rehydration)
    // -------------------------------------------------

    /**
     * Recreates an existing Membership from persistence.
     * No extra validations beyond basic null checks (assumes data consistency).
     */
    public static Membership recreate(IdValue id,
                                      IdValue tenantId,
                                      IdValue communityId,
                                      IdValue userId,
                                      CommunityRole role,
                                      MembershipStatus status,
                                      Instant since,
                                      Instant deactivatedAt,
                                      Instant createdAt,
                                      Instant updatedAt) {

        Objects.requireNonNull(id, "Membership id is required");
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(role, "role is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(since, "since is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");

        return new Membership(
                id,
                tenantId,
                communityId,
                userId,
                role,
                status,
                since,
                deactivatedAt,
                createdAt,
                updatedAt,
                false // no domain validation on recreate
        );
    }
}