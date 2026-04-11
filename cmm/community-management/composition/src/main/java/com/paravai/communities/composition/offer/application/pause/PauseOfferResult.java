package com.paravai.communities.composition.offer.application.pause;

import com.paravai.communities.composition.offer.port.OfferSummary;

/**
 * Result of PauseOffer use case.
 *
 * Notes:
 * - wraps the updated OfferSummary returned by Offer module
 * - does not expose domain aggregate
 * - consistent with other composition results (e.g. C3)
 */
public record PauseOfferResult(
        OfferSummary offer
) {

    public static PauseOfferResult updated(OfferSummary offer) {
        return new PauseOfferResult(offer);
    }
}