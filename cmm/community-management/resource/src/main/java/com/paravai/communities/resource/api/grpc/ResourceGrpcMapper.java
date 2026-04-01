package com.paravai.communities.resource.api.grpc;

import com.paravai.communities.contracts.grpc.resource.v1.GetOwnedResourceResponse;
import com.paravai.communities.resource.domain.model.Resource;

public final class ResourceGrpcMapper {

    private ResourceGrpcMapper() {
    }

    public static GetOwnedResourceResponse toGetOwnedResourceResponse(Resource resource) {

        return GetOwnedResourceResponse.newBuilder()
                .setResourceId(resource.id().value())
                .setOwnerId(resource.ownerId().value())
                .setTitle(resource.title().value())
                .build();
    }
}