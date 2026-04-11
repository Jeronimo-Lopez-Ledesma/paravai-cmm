package com.paravai.communities.composition.api.rest.v1.dto;

import com.paravai.communities.composition.offer.port.OfferSummary;
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
    private String availabilityStatusCode;
    private boolean locked;

    private Instant createdAt;
    private Instant updatedAt;

    public static OfferResponse fromSummary(OfferSummary offer) {
        return OfferResponse.builder()
                .id(offer.offerId())
                .tenantId(offer.tenantId())
                .communityId(offer.communityId())
                .resourceId(offer.resourceId())
                .ownerId(offer.ownerId())
                .exchangeTypeCode(offer.exchangeTypeCode())
                .description(offer.description())
                .statusCode(offer.statusCode())
                .availabilityStatusCode(offer.availabilityStatusCode())
                .locked(offer.locked())
                .createdAt(offer.createdAt())
                .updatedAt(offer.updatedAt())
                .build();
    }
}