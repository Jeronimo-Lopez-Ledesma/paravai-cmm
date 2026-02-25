package com.paravai.communities.community.domain.exception;

import com.paravai.foundation.domain.exception.CustomException;
import com.paravai.foundation.domain.value.IdValue;
import org.springframework.http.HttpStatus;

import java.util.Objects;

public final class DuplicateCommunityBusinessIdentityException extends CustomException {

    private final IdValue tenantId;
    private final String slug;

    public DuplicateCommunityBusinessIdentityException(IdValue tenantId, String slug) {
        super(
                "community.error.duplicateBusinessIdentity",
                HttpStatus.CONFLICT,
                tenantId != null ? tenantId.value() : null,
                slug
        );
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        this.slug = Objects.requireNonNull(slug, "slug");
    }

    public IdValue tenantId() { return tenantId; }
    public String slug() { return slug; }
}