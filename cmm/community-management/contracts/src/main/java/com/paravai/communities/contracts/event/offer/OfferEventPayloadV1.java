package com.paravai.communities.contracts.event.offer;

import java.time.Instant;

public record OfferEventPayloadV1(
        String offerId,
        String tenantId,
        String communityId,
        String resourceId,
        String ownerId,
        String exchangeTypeCode,
        String description,
        String statusCode,
        Instant createdAt,
        Instant updatedAt
) {}