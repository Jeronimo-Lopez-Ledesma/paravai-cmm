package com.paravai.communities.composition.offer.application.publish;

import com.paravai.communities.offer.domain.model.Offer;

import java.util.Objects;

/**
 * Result of PublishOffer use case (composition layer).
 *
 * Notes:
 * - This is a thin wrapper around Offer
 * - No additional state is required for MVP
 */
public final class PublishOfferResult {

    private final Offer offer;

    private PublishOfferResult(Offer offer) {
        this.offer = Objects.requireNonNull(offer, "offer is required");
    }

    public static PublishOfferResult created(Offer offer) {
        return new PublishOfferResult(offer);
    }

    public Offer offer() {
        return offer;
    }
}