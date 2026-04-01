package com.paravai.communities.composition.offer.port;

import reactor.core.publisher.Mono;

/**
 * Outbound port used by composition/orchestration layer
 * to execute commands in Offer module.
 */
public interface OfferCommandPort {

    Mono<OfferSummary> createOffer(CreateOfferCommand command);
}