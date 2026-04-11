package com.paravai.communities.offer.api.grpc;

import com.paravai.communities.contracts.grpc.offer.v1.CreateOfferResponse;
import com.paravai.communities.contracts.grpc.offer.v1.UpdateOfferAvailabilityResponse;
import com.paravai.communities.contracts.grpc.offer.v1.PauseOfferResponse;
import com.paravai.communities.offer.domain.model.Offer;

public final class OfferGrpcMapper {

    private OfferGrpcMapper() {
    }

    public static CreateOfferResponse toCreateOfferResponse(Offer offer) {

        return CreateOfferResponse.newBuilder()
                .setOfferId(offer.id().value())
                .setTenantId(offer.tenantId().value())
                .setCommunityId(offer.communityId().value())
                .setResourceId(offer.resourceId().value())
                .setOwnerId(offer.ownerId().value())
                .setExchangeTypeCode(offer.exchangeType().value())
                .setStatusCode(offer.status().value())
                .setCreatedAt(offer.createdAt().getInstant().toString())
                .setUpdatedAt(offer.updatedAt().getInstant().toString())
                .setDescription(offer.description().orElse(""))
                .build();
    }

    public static UpdateOfferAvailabilityResponse toUpdateOfferAvailabilityResponse(Offer offer) {

        return UpdateOfferAvailabilityResponse.newBuilder()
                .setOfferId(offer.id().value())
                .setTenantId(offer.tenantId().value())
                .setCommunityId(offer.communityId().value())
                .setResourceId(offer.resourceId().value())
                .setOwnerId(offer.ownerId().value())
                .setExchangeTypeCode(offer.exchangeType().value())
                .setDescription(offer.description().orElse(""))
                .setStatusCode(offer.status().value())
                .setAvailabilityStatusCode(offer.availabilityStatus().code())
                .setLocked(offer.locked())
                .setCreatedAt(offer.createdAt().getInstant().toString())
                .setUpdatedAt(offer.updatedAt().getInstant().toString())
                .build();
    }

    public static PauseOfferResponse toPauseOfferResponse(Offer offer) {

        return PauseOfferResponse.newBuilder()
                .setOfferId(offer.id().value())
                .setTenantId(offer.tenantId().value())
                .setCommunityId(offer.communityId().value())
                .setResourceId(offer.resourceId().value())
                .setOwnerId(offer.ownerId().value())
                .setExchangeTypeCode(offer.exchangeType().value())
                .setDescription(offer.description().orElse(""))
                .setStatusCode(offer.status().value())
                .setAvailabilityStatusCode(offer.availabilityStatus().code())
                .setLocked(offer.locked())
                .setCreatedAt(offer.createdAt().getInstant().toString())
                .setUpdatedAt(offer.updatedAt().getInstant().toString())
                .build();
    }
}