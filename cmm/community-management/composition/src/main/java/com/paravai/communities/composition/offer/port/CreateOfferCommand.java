package com.paravai.communities.composition.offer.port;

public record CreateOfferCommand(
        String tenantId,
        String communityId,
        String resourceId,
        String ownerId,
        String exchangeType,
        String description
) {}