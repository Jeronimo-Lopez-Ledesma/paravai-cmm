package com.paravai.communities.composition.offer.port;

public record UpdateOfferAvailabilityCommand(
        String offerId,
        String availabilityStatusCode
) {}