package com.paravai.communities.offer.domain.model;

import com.paravai.communities.offer.domain.value.ExchangeTypeValue;
import com.paravai.communities.offer.domain.value.OfferStatusValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.TimestampValue;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root: Offer
 *
 * Represents a published offer of a resource inside a community.
 *
 * MVP scope:
 * - one offer belongs to exactly one community
 * - one offer references exactly one resource
 * - one offer is owned by exactly one user
 * - the offer starts in ACTIVE status
 * - no pricing
 * - no geolocation
 * - no reservation calendar
 *
 * Aggregate-level invariants:
 * - every Offer must have a stable identity
 * - every Offer must belong to a tenant
 * - every Offer must reference a community
 * - every Offer must reference a resource
 * - every Offer must have an ownerId
 * - every Offer must have an exchangeType
 * - every Offer must have a status
 * - createdAt cannot be after updatedAt
 *
 * Repository / application-level invariants (NOT enforceable from a single aggregate instance):
 * - membership.status must be ACTIVE for the owner in the target community
 * - resource.ownerId must match the authenticated user
 * - exchangeType must be allowed by community rules
 * - there can be at most one ACTIVE offer by (tenantId, communityId, resourceId)
 */
public final class Offer implements Serializable {

    private final IdValue id;

    private final IdValue tenantId;
    private final IdValue communityId;
    private final IdValue resourceId;
    private final IdValue ownerId;

    private final ExchangeTypeValue exchangeType;

    /**
     * Optional free-text offer description.
     */
    private String description;

    /**
     * MVP lifecycle:
     * - ACTIVE
     * - PAUSED
     * - WITHDRAWN
     */
    private OfferStatusValue status;

    private final TimestampValue createdAt;
    private TimestampValue updatedAt;

    /**
     * Constructor intended for OfferFactory.create(...) / recreate(...).
     */
    Offer(IdValue id,
          IdValue tenantId,
          IdValue communityId,
          IdValue resourceId,
          IdValue ownerId,
          ExchangeTypeValue exchangeType,
          String description,
          OfferStatusValue status,
          TimestampValue createdAt,
          TimestampValue updatedAt,
          boolean validate) {

        this.id = Objects.requireNonNull(id, "Offer id is required");

        this.tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        this.communityId = Objects.requireNonNull(communityId, "communityId is required");
        this.resourceId = Objects.requireNonNull(resourceId, "resourceId is required");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId is required");

        this.exchangeType = Objects.requireNonNull(exchangeType, "exchangeType is required");
        this.description = normalizeDescription(description);
        this.status = Objects.requireNonNull(status, "status is required");

        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");

        if (validate) {
            validateInvariants(Clock.systemUTC());
        }
    }

    /**
     * Pauses an ACTIVE offer.
     *
     * Covered invariants:
     * - only ACTIVE offers can transition to PAUSED
     * - WITHDRAWN offers cannot be paused
     *
     * Idempotency:
     * - if already PAUSED, returns false
     */
    public boolean pause(TimestampValue when) {
        if (OfferStatusValue.PAUSED.equals(status)) {
            return false;
        }

        if (OfferStatusValue.WITHDRAWN.equals(status)) {
            throw new IllegalStateException("WITHDRAWN offers cannot be paused");
        }

        if (!OfferStatusValue.ACTIVE.equals(status)) {
            throw new IllegalStateException("Only ACTIVE offers can be paused");
        }

        TimestampValue effectiveWhen = (when != null ? when : TimestampValue.now());

        this.status = OfferStatusValue.PAUSED;
        touch(effectiveWhen);
        validateInvariants(Clock.systemUTC());
        return true;
    }

    /**
     * Withdraws an offer.
     *
     * Covered invariants:
     * - ACTIVE -> WITHDRAWN is valid
     * - PAUSED -> WITHDRAWN is valid
     * - WITHDRAWN -> WITHDRAWN is idempotent
     */
    public boolean withdraw(TimestampValue when) {
        if (OfferStatusValue.WITHDRAWN.equals(status)) {
            return false;
        }

        if (!OfferStatusValue.ACTIVE.equals(status) && !OfferStatusValue.PAUSED.equals(status)) {
            throw new IllegalStateException("Only ACTIVE or PAUSED offers can be withdrawn");
        }

        TimestampValue effectiveWhen = (when != null ? when : TimestampValue.now());

        this.status = OfferStatusValue.WITHDRAWN;
        touch(effectiveWhen);
        validateInvariants(Clock.systemUTC());
        return true;
    }

    public boolean isActive() {
        return OfferStatusValue.ACTIVE.equals(status);
    }

    public boolean isPaused() {
        return OfferStatusValue.PAUSED.equals(status);
    }

    public boolean isWithdrawn() {
        return OfferStatusValue.WITHDRAWN.equals(status);
    }

    public boolean isOwnedBy(IdValue userId) {
        return ownerId.equals(userId);
    }

    private void validateInvariants(Clock clock) {
        Instant now = clock.instant();

        if (createdAt.isAfter(updatedAt)) {
            throw new IllegalStateException("createdAt cannot be after updatedAt");
        }

        if (createdAt.getInstant().isAfter(now)) {
            throw new IllegalArgumentException("createdAt cannot be in the future");
        }

        if (updatedAt.getInstant().isAfter(now)) {
            throw new IllegalArgumentException("updatedAt cannot be in the future");
        }
    }

    private void touch(TimestampValue when) {
        this.updatedAt = (when != null ? when : TimestampValue.now());
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalized = description.trim();
        return normalized.isEmpty() ? null : normalized;
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

    public IdValue resourceId() {
        return resourceId;
    }

    public IdValue ownerId() {
        return ownerId;
    }

    public ExchangeTypeValue exchangeType() {
        return exchangeType;
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public OfferStatusValue status() {
        return status;
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
        if (!(o instanceof Offer that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Offer{id=%s, tenantId=%s, communityId=%s, resourceId=%s, ownerId=%s, exchangeType=%s, status=%s}"
                .formatted(
                        id.value(),
                        tenantId.value(),
                        communityId.value(),
                        resourceId.value(),
                        ownerId.value(),
                        exchangeType.value(),
                        status.value()
                );
    }
}