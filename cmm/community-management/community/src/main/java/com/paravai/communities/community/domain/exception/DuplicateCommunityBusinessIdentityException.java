package com.paravai.communities.community.domain.exception;

import com.paravai.foundation.domain.value.IdValue;

import java.util.Objects;

public final class DuplicateCommunityBusinessIdentityException extends RuntimeException {

    private final IdValue tenantId;
    private final String slug;

    public DuplicateCommunityBusinessIdentityException(IdValue tenantId, String slug, Throwable cause) {
        super("Duplicate community business identity for tenantId=%s, slug=%s"
                .formatted(
                        tenantId != null ? tenantId.value() : "null",
                        slug
                ), cause);
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.slug = Objects.requireNonNull(slug, "slug");
    }

    public DuplicateCommunityBusinessIdentityException(IdValue tenantId, String slug) {
        this(tenantId, slug, null);
    }

    public IdValue tenantId() { return tenantId; }
    public String slug() { return slug; }
}