package com.paravai.communities.membership.api.grpc;

import com.paravai.communities.contracts.grpc.membership.v1.GetMembershipStatusForCommunityResponse;
import com.paravai.communities.membership.domain.model.Membership;

public final class MembershipGrpcMapper {

    private MembershipGrpcMapper() {
    }

    public static GetMembershipStatusForCommunityResponse toGetMembershipStatusForCommunityResponse(
            Membership membership
    ) {
        return GetMembershipStatusForCommunityResponse.newBuilder()
                .setMembershipId(membership.id().value())
                .setStatusCode(membership.status().getCode())
                .setRoleCode(membership.role().map(r -> r.getCode()).orElse(""))
                .build();
    }
}