package com.paravai.communities.resource.application.query.get;

import com.paravai.communities.resource.domain.model.Resource;

import java.util.Objects;

public final class GetResourceResult {

    private final Resource resource;

    private GetResourceResult(Resource resource) {
        this.resource = Objects.requireNonNull(resource, "resource is required");
    }

    public static GetResourceResult found(Resource resource) {
        return new GetResourceResult(resource);
    }

    public Resource resource() {
        return resource;
    }
}