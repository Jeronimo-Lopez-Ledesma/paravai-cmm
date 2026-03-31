package com.paravai.communities.offer.application.command.create;

import com.paravai.communities.offer.domain.model.Offer;

import java.util.Objects;

public final class CreateOfferResult {

    private final Offer offer;
    private final boolean created;

    private CreateOfferResult(Offer offer, boolean created) {
        this.offer = Objects.requireNonNull(offer, "offer is required");
        this.created = created;
    }

    public static CreateOfferResult created(Offer offer) {
        return new CreateOfferResult(offer, true);
    }

    public Offer offer() {
        return offer;
    }

    public boolean created() {
        return created;
    }
}