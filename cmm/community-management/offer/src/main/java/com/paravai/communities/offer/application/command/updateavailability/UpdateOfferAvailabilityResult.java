package com.paravai.communities.offer.application.command.updateavailability;

import com.paravai.communities.offer.domain.model.Offer;

import java.util.Objects;

public final class UpdateOfferAvailabilityResult {

    private final Offer offer;
    private final boolean updated;

    private UpdateOfferAvailabilityResult(Offer offer, boolean updated) {
        this.offer = Objects.requireNonNull(offer, "offer is required");
        this.updated = updated;
    }

    public static UpdateOfferAvailabilityResult updated(Offer offer) {
        return new UpdateOfferAvailabilityResult(offer, true);
    }

    public static UpdateOfferAvailabilityResult unchanged(Offer offer) {
        return new UpdateOfferAvailabilityResult(offer, false);
    }

    public Offer offer() {
        return offer;
    }

    public boolean updated() {
        return updated;
    }
}