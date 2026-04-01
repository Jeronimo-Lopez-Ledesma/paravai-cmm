package com.paravai.communities.community.api.grpc;

import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.contracts.grpc.community.v1.GetCommunityOfferPolicyResponse;

public final class CommunityGrpcMapper {

    private CommunityGrpcMapper() {
    }

    public static GetCommunityOfferPolicyResponse toGetCommunityOfferPolicyResponse(
            Community community
    ) {

        GetCommunityOfferPolicyResponse.Builder builder =
                GetCommunityOfferPolicyResponse.newBuilder()
                        .setCommunityId(community.id().value());

        community.allowedExchangeTypes()
                .forEach(type -> builder.addAllowedExchangeTypes(type));

        return builder.build();
    }
}