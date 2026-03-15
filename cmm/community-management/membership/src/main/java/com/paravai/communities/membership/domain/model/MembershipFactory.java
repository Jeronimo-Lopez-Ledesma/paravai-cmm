package com.paravai.communities.membership.domain.model;

import com.paravai.communities.membership.domain.value.CommunityRoleValue;
import com.paravai.communities.membership.domain.value.MembershipStatusValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.TimestampValue;

import java.util.Objects;

/**
 * Factory: MembershipFactory
 *
 * Encapsulates valid Membership creation and reconstruction.
 *
 * Methods:
 * - createFounder(): creates the initial ACTIVE ADMIN membership for the community creator
 * - createActiveAdmin(): creates an ACTIVE ADMIN membership
 * - createActiveMember(): creates an ACTIVE MEMBER membership
 * - createPendingRequest(): creates a PENDING membership request
 * - recreate(): rehydrates an existing membership from persistence
 */
public final class MembershipFactory {

    private MembershipFactory() {
        throw new IllegalStateException("Factory class — not instantiable");
    }

    // -------------------------------------------------
    // Creation
    // -------------------------------------------------

    /**
     * Creates the initial founder membership for the community creator.
     *
     * Covered invariants:
     * - ACTIVE membership must have role
     * - ACTIVE membership must have decidedAt
     * - ACTIVE membership must not have rejectionReason
     * - requestedAt / createdAt / updatedAt are initialized consistently
     */
    public static Membership createFounder(IdValue tenantId,
                                           IdValue communityId,
                                           IdValue userId) {
        return createActiveAdmin(tenantId, communityId, userId);
    }

    /**
     * Creates a generic ACTIVE ADMIN membership.
     *
     * Covered invariants:
     * - ACTIVE membership must have role
     * - ACTIVE membership must have decidedAt
     * - ACTIVE membership must not have rejectionReason
     */
    public static Membership createActiveAdmin(IdValue tenantId,
                                               IdValue communityId,
                                               IdValue userId) {

        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(userId, "userId is required");

        TimestampValue now = TimestampValue.now();

        return new Membership(
                IdValue.generate(),
                tenantId,
                communityId,
                userId,
                CommunityRoleValue.ADMIN,
                MembershipStatusValue.ACTIVE,
                null,
                now,   // requestedAt
                now,   // decidedAt
                now,   // createdAt
                now,   // updatedAt
                true
        );
    }

    /**
     * Creates a generic ACTIVE MEMBER membership.
     *
     * Useful for admin tools, imports, migrations or controlled bootstrap scenarios.
     *
     * Covered invariants:
     * - ACTIVE membership must have role
     * - ACTIVE membership must have decidedAt
     * - ACTIVE membership must not have rejectionReason
     */
    public static Membership createActiveMember(IdValue tenantId,
                                                IdValue communityId,
                                                IdValue userId) {

        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(userId, "userId is required");

        TimestampValue now = TimestampValue.now();

        return new Membership(
                IdValue.generate(),
                tenantId,
                communityId,
                userId,
                CommunityRoleValue.MEMBER,
                MembershipStatusValue.ACTIVE,
                null,
                now,   // requestedAt
                now,   // decidedAt
                now,   // createdAt
                now,   // updatedAt
                true
        );
    }

    /**
     * Creates a PENDING membership request.
     *
     * This is the factory method that should be used for EPIC B / B1:
     * RequestMembership.
     *
     * Covered invariants:
     * - PENDING membership cannot have role
     * - PENDING membership cannot have decidedAt
     * - PENDING membership cannot have rejectionReason
     */
    public static Membership createPendingRequest(IdValue tenantId,
                                                  IdValue communityId,
                                                  IdValue userId) {

        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(userId, "userId is required");

        TimestampValue now = TimestampValue.now();

        return new Membership(
                IdValue.generate(),
                tenantId,
                communityId,
                userId,
                null,
                MembershipStatusValue.PENDING,
                null,
                now,   // requestedAt
                null,  // decidedAt
                now,   // createdAt
                now,   // updatedAt
                true
        );
    }

    // -------------------------------------------------
    // Reconstruction (rehydration)
    // -------------------------------------------------

    /**
     * Recreates an existing Membership from persistence.
     *
     * Assumes the persisted state was already validated when written.
     * This method performs only structural mandatory checks and delegates
     * semantic consistency to the persistence/application boundaries.
     *
     * Covered invariants:
     * - mandatory structural fields are present
     *
     * NOT covered here:
     * - repository/application-level invariants such as uniqueness by
     *   (tenantId, communityId, userId)
     */
    public static Membership recreate(IdValue id,
                                      IdValue tenantId,
                                      IdValue communityId,
                                      IdValue userId,
                                      CommunityRoleValue role,
                                      MembershipStatusValue status,
                                      String rejectionReason,
                                      TimestampValue requestedAt,
                                      TimestampValue decidedAt,
                                      TimestampValue createdAt,
                                      TimestampValue updatedAt) {

        Objects.requireNonNull(id, "Membership id is required");
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(requestedAt, "requestedAt is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");

        return new Membership(
                id,
                tenantId,
                communityId,
                userId,
                role,
                status,
                rejectionReason,
                requestedAt,
                decidedAt,
                createdAt,
                updatedAt,
                false
        );
    }
}