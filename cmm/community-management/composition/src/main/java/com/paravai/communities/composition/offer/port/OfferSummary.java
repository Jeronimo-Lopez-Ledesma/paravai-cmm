package com.paravai.communities.composition.offer.port;

import java.time.Instant;

public record OfferSummary(
        String offerId,
        String tenantId,
        String communityId,
        String resourceId,
        String ownerId,
        String exchangeType,
        String description,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}