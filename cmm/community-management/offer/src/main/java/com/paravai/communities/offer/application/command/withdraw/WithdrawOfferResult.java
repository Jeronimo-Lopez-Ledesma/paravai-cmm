package com.paravai.communities.offer.application.command.withdraw;

import com.paravai.communities.offer.domain.model.Offer;

import java.util.Objects;

/**
 * Result of withdrawing an offer.
 *
 * Encapsulates whether the operation produced a state change
 * and exposes the resulting aggregate.
 */
public final class WithdrawOfferResult {

    private final Offer offer;
    private final boolean changed;

    private WithdrawOfferResult(Offer offer, boolean changed) {
        this.offer = Objects.requireNonNull(offer, "offer is required");
        this.changed = changed;
    }

    /**
     * Factory for a successful state transition (ACTIVE/PAUSED -> WITHDRAWN).
     */
    public static WithdrawOfferResult updated(Offer offer) {
        return new WithdrawOfferResult(offer, true);
    }

    /**
     * Factory for idempotent case (already WITHDRAWN).
     */
    public static WithdrawOfferResult unchanged(Offer offer) {
        return new WithdrawOfferResult(offer, false);
    }

    public Offer offer() {
        return offer;
    }

    public boolean changed() {
        return changed;
    }
}