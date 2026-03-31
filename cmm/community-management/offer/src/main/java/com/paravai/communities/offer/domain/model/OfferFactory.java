package com.paravai.communities.offer.domain.model;

import com.paravai.communities.offer.domain.value.ExchangeTypeValue;
import com.paravai.communities.offer.domain.value.OfferStatusValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.TimestampValue;

import java.util.Objects;

/**
 * Factory: OfferFactory
 *
 * Encapsulates valid Offer creation and reconstruction.
 *
 * Methods:
 * - create(): creates a new ACTIVE offer
 * - recreate(): rehydrates an existing offer from persistence
 */
public final class OfferFactory {

    private OfferFactory() {
        throw new IllegalStateException("Factory class — not instantiable");
    }

    // -------------------------------------------------
    // Creation
    // -------------------------------------------------

    /**
     * Creates a new ACTIVE offer.
     *
     * Covered invariants:
     * - offer must have id
     * - offer must have tenantId
     * - offer must have communityId
     * - offer must have resourceId
     * - offer must have ownerId
     * - offer must have exchangeType
     * - offer starts in ACTIVE state
     * - createdAt / updatedAt are initialized consistently
     */
    public static Offer create(IdValue tenantId,
                               IdValue communityId,
                               IdValue resourceId,
                               IdValue ownerId,
                               ExchangeTypeValue exchangeType,
                               String description) {

        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(resourceId, "resourceId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");
        Objects.requireNonNull(exchangeType, "exchangeType is required");

        TimestampValue now = TimestampValue.now();

        return new Offer(
                IdValue.generate(),
                tenantId,
                communityId,
                resourceId,
                ownerId,
                exchangeType,
                description,
                OfferStatusValue.ACTIVE,
                now,
                now,
                true
        );
    }

    // -------------------------------------------------
    // Reconstruction (rehydration)
    // -------------------------------------------------

    /**
     * Recreates an existing Offer from persistence.
     *
     * Assumes the persisted state was already validated when written.
     * This method performs only structural mandatory checks.
     */
    public static Offer recreate(IdValue id,
                                 IdValue tenantId,
                                 IdValue communityId,
                                 IdValue resourceId,
                                 IdValue ownerId,
                                 ExchangeTypeValue exchangeType,
                                 String description,
                                 OfferStatusValue status,
                                 TimestampValue createdAt,
                                 TimestampValue updatedAt) {

        Objects.requireNonNull(id, "Offer id is required");
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(resourceId, "resourceId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");
        Objects.requireNonNull(exchangeType, "exchangeType is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");

        return new Offer(
                id,
                tenantId,
                communityId,
                resourceId,
                ownerId,
                exchangeType,
                description,
                status,
                createdAt,
                updatedAt,
                false
        );
    }
}