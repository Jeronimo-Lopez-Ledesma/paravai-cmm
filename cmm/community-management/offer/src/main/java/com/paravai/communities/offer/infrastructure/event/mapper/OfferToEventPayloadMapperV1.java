package com.paravai.communities.offer.infrastructure.event.mapper;

import com.paravai.communities.contracts.event.offer.OfferEventPayloadV1;
import com.paravai.communities.offer.domain.model.Offer;
import org.springframework.stereotype.Component;

@Component
public class OfferToEventPayloadMapperV1 {

    public OfferEventPayloadV1 map(Offer offer) {

        if (offer == null) {
            throw new IllegalArgumentException("Offer must not be null");
        }

        return new OfferEventPayloadV1(
                offer.id().value(),
                offer.tenantId().value(),
                offer.communityId().value(),
                offer.resourceId().value(),
                offer.ownerId().value(),
                offer.exchangeType().value(),
                offer.description().orElse(null),
                offer.status().value(),
                offer.createdAt().getInstant(),
                offer.updatedAt().getInstant()
        );
    }
}