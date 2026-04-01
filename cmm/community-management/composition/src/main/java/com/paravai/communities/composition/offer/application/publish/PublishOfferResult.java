package com.paravai.communities.composition.offer.application.publish;

import com.paravai.communities.composition.offer.port.OfferSummary;

import java.util.Objects;

/**
 * Result of PublishOffer use case (composition layer).
 *
 * Notes:
 * - Composition layer must not expose Offer domain aggregate directly
 * - It returns OfferSummary, which is a composition-facing model
 */
public final class PublishOfferResult {

    private final OfferSummary offer;

    private PublishOfferResult(OfferSummary offer) {
        this.offer = Objects.requireNonNull(offer, "offer is required");
    }

    public static PublishOfferResult created(OfferSummary offer) {
        return new PublishOfferResult(offer);
    }

    public OfferSummary offer() {
        return offer;
    }
}