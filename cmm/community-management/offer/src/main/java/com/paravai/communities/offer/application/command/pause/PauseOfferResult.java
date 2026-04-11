package com.paravai.communities.offer.application.command.pause;

import com.paravai.communities.offer.domain.model.Offer;

import java.util.Objects;

public final class PauseOfferResult {

    private final Offer offer;
    private final boolean updated;

    private PauseOfferResult(Offer offer, boolean updated) {
        this.offer = Objects.requireNonNull(offer, "offer is required");
        this.updated = updated;
    }

    public static PauseOfferResult updated(Offer offer) {
        return new PauseOfferResult(offer, true);
    }

    public static PauseOfferResult unchanged(Offer offer) {
        return new PauseOfferResult(offer, false);
    }

    public Offer offer() {
        return offer;
    }

    public boolean updated() {
        return updated;
    }
}