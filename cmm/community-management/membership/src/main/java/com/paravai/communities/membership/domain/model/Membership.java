package com.paravai.communities.membership.domain.model;

import com.paravai.communities.membership.domain.value.CommunityRoleValue;
import com.paravai.communities.membership.domain.value.MembershipStatusValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.TimestampValue;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root: Membership
 *
 * Represents the relationship between a user and a community.
 *
 * MVP lifecycle:
 * - PENDING
 * - ACTIVE
 * - REJECTED
 *
 * This aggregate models both:
 * - access request
 * - active membership
 *
 * Repository / application-level invariants (NOT enforceable from a single aggregate instance):
 * - uniqueness by (tenantId, communityId, userId)
 * - only one effective relationship per (tenantId, communityId, userId)
 * - community must keep at least one ACTIVE ADMIN
 */
public final class Membership implements Serializable {

    private static final int MAX_REJECTION_REASON_LENGTH = 500;

    private final IdValue id;

    private final IdValue tenantId;
    private final IdValue communityId;
    private final IdValue userId;

    /**
     * Only applicable when status == ACTIVE.
     * Must be null for PENDING and REJECTED.
     */
    private CommunityRoleValue role;

    /**
     * Allowed MVP states:
     * - PENDING
     * - ACTIVE
     * - REJECTED
     */
    private MembershipStatusValue status;

    /**
     * Optional reason when membership request is rejected.
     * Only applicable when status == REJECTED.
     */
    private String rejectionReason;

    /**
     * Instant when the membership request was created.
     */
    private final TimestampValue requestedAt;

    /**
     * Instant when the request was approved or rejected.
     * Must be null while status == PENDING.
     * Must be non-null for ACTIVE and REJECTED.
     */
    private TimestampValue decidedAt;

    private final TimestampValue createdAt;
    private TimestampValue updatedAt;

    /**
     * Constructor intended for MembershipFactory.create(...) / recreate(...).
     */
    Membership(IdValue id,
               IdValue tenantId,
               IdValue communityId,
               IdValue userId,
               CommunityRoleValue role,
               MembershipStatusValue status,
               String rejectionReason,
               TimestampValue requestedAt,
               TimestampValue decidedAt,
               TimestampValue createdAt,
               TimestampValue updatedAt,
               boolean validate) {

        this.id = Objects.requireNonNull(id, "Membership id is required");

        this.tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        this.communityId = Objects.requireNonNull(communityId, "communityId is required");
        this.userId = Objects.requireNonNull(userId, "userId is required");

        this.role = role;
        this.status = Objects.requireNonNull(status, "status is required");
        this.rejectionReason = normalizeReason(rejectionReason);

        this.requestedAt = Objects.requireNonNull(requestedAt, "requestedAt is required");
        this.decidedAt = decidedAt;

        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");

        if (validate) {
            validateInvariants(Clock.systemUTC());
        }
    }

    /**
     * Approves a pending request and activates the membership with the given initial role.
     *
     * Covered invariants:
     * - only PENDING can transition to ACTIVE
     * - ACTIVE memberships must have a role
     * - ACTIVE memberships must not have rejectionReason
     * - ACTIVE memberships must have decidedAt
     *
     * Idempotency:
     * - if already ACTIVE, returns false and does not mutate state
     */
    public boolean approve(CommunityRoleValue initialRole, TimestampValue when) {
        Objects.requireNonNull(initialRole, "initialRole is required");

        if (status == MembershipStatusValue.ACTIVE) {
            return false;
        }

        if (status != MembershipStatusValue.PENDING) {
            throw new IllegalStateException("Only PENDING memberships can be approved");
        }

        TimestampValue effectiveWhen = (when != null ? when : TimestampValue.now());

        this.status = MembershipStatusValue.ACTIVE;
        this.role = initialRole;
        this.rejectionReason = null;
        this.decidedAt = effectiveWhen;

        touch(effectiveWhen);
        validateInvariants(Clock.systemUTC());
        return true;
    }

    /**
     * Rejects a pending request.
     *
     * Covered invariants:
     * - only PENDING can transition to REJECTED
     * - REJECTED memberships must not have role
     * - REJECTED memberships must have decidedAt
     * - rejectionReason is optional, but if present it must be normalized and within max length
     *
     * Idempotency:
     * - if already REJECTED, returns false and does not mutate state
     */
    public boolean reject(String reason, TimestampValue when) {
        if (status == MembershipStatusValue.REJECTED) {
            return false;
        }

        if (status != MembershipStatusValue.PENDING) {
            throw new IllegalStateException("Only PENDING memberships can be rejected");
        }

        TimestampValue effectiveWhen = (when != null ? when : TimestampValue.now());

        this.status = MembershipStatusValue.REJECTED;
        this.role = null;
        this.rejectionReason = normalizeReason(reason);
        this.decidedAt = effectiveWhen;

        touch(effectiveWhen);
        validateInvariants(Clock.systemUTC());
        return true;
    }

    /**
     * Assigns or changes role for an ACTIVE membership.
     *
     * Covered invariants:
     * - only ACTIVE memberships can have role
     * - role must be non-null when status == ACTIVE
     *
     * NOT covered here:
     * - "community must keep at least one ACTIVE ADMIN"
     *   This requires querying other memberships in the same community and
     *   must be enforced by a domain service / application service.
     *
     * Idempotency:
     * - if role is already the requested one, returns false
     */
    public boolean changeRole(CommunityRoleValue newRole, TimestampValue when) {
        Objects.requireNonNull(newRole, "newRole is required");

        if (status != MembershipStatusValue.ACTIVE) {
            throw new IllegalStateException("Role can only be changed for ACTIVE memberships");
        }

        if (newRole.equals(this.role)) {
            return false;
        }

        TimestampValue effectiveWhen = (when != null ? when : TimestampValue.now());

        this.role = newRole;
        touch(effectiveWhen);
        validateInvariants(Clock.systemUTC());
        return true;
    }

    /**
     * Convenience method for approval idempotency checks at application level.
     *
     * Covered invariant:
     * - reflects current aggregate state only
     */
    public boolean isPending() {
        return status == MembershipStatusValue.PENDING;
    }

    /**
     * Convenience method for application/domain policies.
     *
     * Covered invariant:
     * - ACTIVE memberships are the only memberships considered real members
     */
    public boolean isActive() {
        return status == MembershipStatusValue.ACTIVE;
    }

    /**
     * Convenience method for application/domain policies.
     */
    public boolean isRejected() {
        return status == MembershipStatusValue.REJECTED;
    }

    /**
     * Returns true when membership is ACTIVE and role is ADMIN.
     *
     * Covered invariants:
     * - role only applies to ACTIVE memberships
     */
    public boolean isActiveAdmin() {
        return status == MembershipStatusValue.ACTIVE
                && CommunityRoleValue.ADMIN.equals(role);
    }

    private void validateInvariants(Clock clock) {
        Instant now = clock.instant();

        if (createdAt.isAfter(updatedAt)) {
            throw new IllegalStateException("createdAt cannot be after updatedAt");
        }

        if (requestedAt.isAfter(updatedAt)) {
            throw new IllegalStateException("requestedAt cannot be after updatedAt");
        }

        if (requestedAt.getInstant().isAfter(now)) {
            throw new IllegalArgumentException("requestedAt cannot be in the future");
        }

        if (createdAt.getInstant().isAfter(now)) {
            throw new IllegalArgumentException("createdAt cannot be in the future");
        }

        if (updatedAt.getInstant().isAfter(now)) {
            throw new IllegalArgumentException("updatedAt cannot be in the future");
        }

        if (decidedAt != null) {
            if (decidedAt.getInstant().isBefore(requestedAt.getInstant())) {
                throw new IllegalArgumentException("decidedAt cannot be before requestedAt");
            }

            if (decidedAt.getInstant().isAfter(now)) {
                throw new IllegalArgumentException("decidedAt cannot be in the future");
            }

            if (decidedAt.isAfter(updatedAt)) {
                throw new IllegalStateException("decidedAt cannot be after updatedAt");
            }
        }

        if (MembershipStatusValue.PENDING.equals(status)) {
            if (role != null) {
                throw new IllegalStateException("PENDING membership cannot have role");
            }
            if (decidedAt != null) {
                throw new IllegalStateException("PENDING membership cannot have decidedAt");
            }
            if (rejectionReason != null) {
                throw new IllegalStateException("PENDING membership cannot have rejectionReason");
            }
            return;
        }

        if (MembershipStatusValue.ACTIVE.equals(status)) {
            if (role == null) {
                throw new IllegalStateException("ACTIVE membership must have role");
            }
            if (decidedAt == null) {
                throw new IllegalStateException("ACTIVE membership must have decidedAt");
            }
            if (rejectionReason != null) {
                throw new IllegalStateException("ACTIVE membership cannot have rejectionReason");
            }
            return;
        }

        if (MembershipStatusValue.REJECTED.equals(status)) {
            if (role != null) {
                throw new IllegalStateException("REJECTED membership cannot have role");
            }
            if (decidedAt == null) {
                throw new IllegalStateException("REJECTED membership must have decidedAt");
            }
            if (rejectionReason != null && rejectionReason.length() > MAX_REJECTION_REASON_LENGTH) {
                throw new IllegalArgumentException("rejectionReason exceeds maximum allowed length");
            }
            return;
        }

        throw new IllegalStateException("Unsupported membership status: " + status);
    }


    private void touch(TimestampValue when) {
        this.updatedAt = (when != null ? when : TimestampValue.now());
    }

    private static String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }

        String normalized = reason.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.length() > MAX_REJECTION_REASON_LENGTH) {
            throw new IllegalArgumentException(
                    "rejectionReason cannot exceed " + MAX_REJECTION_REASON_LENGTH + " characters"
            );
        }

        return normalized;
    }

    public IdValue id() {
        return id;
    }

    public IdValue tenantId() {
        return tenantId;
    }

    public IdValue communityId() {
        return communityId;
    }

    public IdValue userId() {
        return userId;
    }

    public Optional<CommunityRoleValue> role() {
        return Optional.ofNullable(role);
    }

    public MembershipStatusValue status() {
        return status;
    }

    public Optional<String> rejectionReason() {
        return Optional.ofNullable(rejectionReason);
    }

    public TimestampValue requestedAt() {
        return requestedAt;
    }

    public Optional<TimestampValue> decidedAt() {
        return Optional.ofNullable(decidedAt);
    }

    public TimestampValue createdAt() {
        return createdAt;
    }

    public TimestampValue updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Membership that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Membership{id=%s, tenantId=%s, communityId=%s, userId=%s, status=%s, role=%s}"
                .formatted(
                        id.value(),
                        tenantId.value(),
                        communityId.value(),
                        userId.value(),
                        status,
                        role != null ? role : "null"
                );
    }
}