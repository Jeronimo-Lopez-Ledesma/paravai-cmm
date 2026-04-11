package com.paravai.communities.composition.offer.port;

import java.time.Instant;

public record OfferSummary(
        String offerId,
        String tenantId,
        String communityId,
        String resourceId,
        String ownerId,
        String exchangeTypeCode,
        String description,
        String statusCode,
        String availabilityStatusCode,
        boolean locked,
        Instant createdAt,
        Instant updatedAt
) {}