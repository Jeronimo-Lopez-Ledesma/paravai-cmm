package com.paravai.communities.composition.offer.application.updateavailability;

import com.paravai.communities.composition.offer.port.OfferSummary;

import java.util.Objects;

public final class UpdateOfferAvailabilityResult {

    private final OfferSummary offer;

    private UpdateOfferAvailabilityResult(OfferSummary offer) {
        this.offer = Objects.requireNonNull(offer, "offer is required");
    }

    public static UpdateOfferAvailabilityResult updated(OfferSummary offer) {
        return new UpdateOfferAvailabilityResult(offer);
    }

    public OfferSummary offer() {
        return offer;
    }
}