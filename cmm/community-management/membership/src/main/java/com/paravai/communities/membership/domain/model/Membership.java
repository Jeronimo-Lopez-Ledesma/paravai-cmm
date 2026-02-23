package com.paravai.communities.membership.domain.model;

import com.paravai.communities.membership.domain.value.CommunityRoleValue;
import com.paravai.communities.membership.domain.value.MembershipStatusValue;
import com.paravai.foundation.domain.value.IdValue;

import java.io.Serializable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Aggregate Root: Membership
 *
 * Relationship between user and community.
 *
 *
 * Uniqueness (repository-level):
 * - (tenantId, communityId, userId) is unique
 */
public final class Membership implements Serializable {

    private final IdValue id;

    private final IdValue tenantId;
    private final IdValue communityId;
    private final IdValue userId;

    private CommunityRoleValue role;
    private MembershipStatusValue status;

    private Instant since;
    private Instant deactivatedAt;

    private final Instant createdAt;
    private Instant updatedAt;

    // -------------------------------------------------
    // Constructor (package-private) — only Factory can call
    // -------------------------------------------------
    Membership(IdValue id,
               IdValue tenantId,
               IdValue communityId,
               IdValue userId,
               CommunityRoleValue role,
               MembershipStatusValue status,
               Instant since,
               Instant deactivatedAt,
               Instant createdAt,
               Instant updatedAt,
               boolean validate) {

        this.id = Objects.requireNonNull(id, "Membership id is required");

        this.tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        this.communityId = Objects.requireNonNull(communityId, "communityId is required");
        this.userId = Objects.requireNonNull(userId, "userId is required");

        this.role = Objects.requireNonNull(role, "role is required");
        this.status = Objects.requireNonNull(status, "status is required");

        this.since = Objects.requireNonNull(since, "since is required");
        this.deactivatedAt = deactivatedAt;

        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");

        if (validate) {
            validateInvariants(Clock.systemUTC());
        }
    }

    // -------------------------------------------------
    // Domain behavior
    // -------------------------------------------------

    public void changeRole(CommunityRoleValue newRole) {
        this.role = Objects.requireNonNull(newRole, "role is required");
        touch();
        validateInvariants(Clock.systemUTC());
    }

    /**
     * Invitation acceptance:
     * - PENDING -> ACTIVE (idempotent)
     */
    public void acceptInvitation() {
        if (status != MembershipStatusValue.PENDING) return;
        this.status = MembershipStatusValue.ACTIVE;
        touch();
        validateInvariants(Clock.systemUTC());
    }

    /**
     * Invitation revocation:
     * - PENDING -> REVOKED (idempotent)
     */
    public void revokeInvitation(Instant when) {
        if (status == MembershipStatusValue.REVOKED) return;

        if (status != MembershipStatusValue.PENDING) {
            throw new IllegalStateException("Only PENDING invitations can be revoked");
        }

        this.status = MembershipStatusValue.REVOKED;
        this.deactivatedAt = (when != null ? when : Instant.now());
        touch();
        validateInvariants(Clock.systemUTC());
    }

    public void deactivate(Instant when) {
        if (status == MembershipStatusValue.INACTIVE) return;

        if (status == MembershipStatusValue.PENDING) {
            throw new IllegalStateException("Cannot deactivate a PENDING invitation; revoke it instead");
        }

        this.status = MembershipStatusValue.INACTIVE;
        this.deactivatedAt = (when != null ? when : Instant.now());
        touch();
        validateInvariants(Clock.systemUTC());
    }

    public void reactivate(Instant newSince) {
        if (status == MembershipStatusValue.ACTIVE) return;

        if (status == MembershipStatusValue.PENDING) {
            throw new IllegalStateException("Cannot reactivate a PENDING invitation; accept it instead");
        }
        if (status == MembershipStatusValue.REVOKED) {
            throw new IllegalStateException("Cannot reactivate a REVOKED invitation");
        }

        Instant effectiveSince = (newSince != null ? newSince : Instant.now());
        if (effectiveSince.isAfter(Instant.now())) {
            throw new IllegalArgumentException("since cannot be in the future");
        }

        this.status = MembershipStatusValue.ACTIVE;
        this.deactivatedAt = null;
        this.since = effectiveSince;

        touch();
        validateInvariants(Clock.systemUTC());
    }

    // -------------------------------------------------
    // Domain queries (testable with Clock)
    // -------------------------------------------------

    public boolean isActive() {
        return isActive(Clock.systemUTC());
    }

    public boolean isActive(Clock clock) {
        if (status != MembershipStatusValue.ACTIVE) return false;
        return !since.isAfter(clock.instant());
    }

    public Duration getActiveDuration() {
        return getActiveDuration(Clock.systemUTC());
    }

    public Duration getActiveDuration(Clock clock) {
        Instant end = (status == MembershipStatusValue.ACTIVE ? clock.instant() : deactivatedAt);
        if (end == null) end = clock.instant();
        return Duration.between(since, end);
    }

    public long getActiveDays() {
        return getActiveDays(Clock.systemUTC());
    }

    public long getActiveDays(Clock clock) {
        Instant end = (status == MembershipStatusValue.ACTIVE ? clock.instant() : deactivatedAt);
        if (end == null) end = clock.instant();
        return ChronoUnit.DAYS.between(since, end);
    }

    // -------------------------------------------------
    // Invariant validation
    // -------------------------------------------------

    private void validateInvariants(Clock clock) {
        if (createdAt.isAfter(updatedAt)) {
            throw new IllegalStateException("createdAt cannot be after updatedAt");
        }

        if (since.isAfter(clock.instant())) {
            throw new IllegalArgumentException("since cannot be in the future");
        }

        if (deactivatedAt != null && deactivatedAt.isBefore(since)) {
            throw new IllegalArgumentException("deactivatedAt cannot be before since");
        }

        // status <-> deactivatedAt consistency
        if (status == MembershipStatusValue.ACTIVE || status == MembershipStatusValue.PENDING) {
            if (deactivatedAt != null) {
                throw new IllegalStateException(status + " membership cannot have deactivatedAt");
            }
        } else {
            if (deactivatedAt == null) {
                throw new IllegalStateException(status + " membership must have deactivatedAt");
            }
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    // -------------------------------------------------
    // Getters
    // -------------------------------------------------

    public IdValue id() { return id; }

    public IdValue tenantId() { return tenantId; }
    public IdValue communityId() { return communityId; }
    public IdValue userId() { return userId; }

    public CommunityRoleValue role() { return role; }
    public MembershipStatusValue status() { return status; }

    public Instant since() { return since; }
    public Instant deactivatedAt() { return deactivatedAt; }

    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    // -------------------------------------------------
    // Identity equality
    // -------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Membership that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Membership{id=%s, tenantId=%s, communityId=%s, userId=%s, role=%s, status=%s}"
                .formatted(id.value(), tenantId.value(), communityId.value(), userId.value(), role, status);
    }
}