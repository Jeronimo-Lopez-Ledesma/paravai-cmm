package com.paravai.communities.resource.application.command.register;

import com.paravai.communities.resource.domain.model.Resource;

import java.util.Objects;

public final class RegisterResourceResult {

    private final Resource resource;
    private final boolean created;

    private RegisterResourceResult(Resource resource, boolean created) {
        this.resource = Objects.requireNonNull(resource, "resource is required");
        this.created = created;
    }

    public static RegisterResourceResult created(Resource resource) {
        return new RegisterResourceResult(resource, true);
    }

    public Resource resource() {
        return resource;
    }

    public boolean created() {
        return created;
    }
}