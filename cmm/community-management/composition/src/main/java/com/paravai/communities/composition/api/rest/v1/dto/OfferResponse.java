package com.paravai.communities.composition.api.rest.v1.dto;

import com.paravai.communities.offer.domain.model.Offer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferResponse {

    private String id;

    private String tenantId;
    private String communityId;
    private String resourceId;
    private String ownerId;

    private String exchangeTypeCode;
    private String description;

    private String statusCode;

    private Instant createdAt;
    private Instant updatedAt;

    public static OfferResponse fromDomain(Offer offer) {
        return OfferResponse.builder()
                .id(offer.id().value())
                .tenantId(offer.tenantId().value())
                .communityId(offer.communityId().value())
                .resourceId(offer.resourceId().value())
                .ownerId(offer.ownerId().value())
                .exchangeTypeCode(offer.exchangeType().value())
                .description(offer.description().orElse(null))
                .statusCode(offer.status().value())
                .createdAt(offer.createdAt().getInstant())
                .updatedAt(offer.updatedAt().getInstant())
                .build();
    }
}