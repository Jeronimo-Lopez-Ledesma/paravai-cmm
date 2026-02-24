package com.paravai.communities.community.api.rest.v1.dto;

import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.model.CommunityFactory;
import com.paravai.foundation.domain.value.IdValue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * REST DTO: CreateCommunityRequest (EPIC A / A1)
 *
 * MVP scope:
 * - name required
 * - description optional (informational)
 *
 * Not included in MVP request:
 * - visibility (defaults to PRIVATE in factory)
 * - rules (A3)
 * - invitations (A4)
 *
 * TenantId and createdBy are NOT taken from the payload; they must come from RequestContext (headers/JWT)
 * in the controller/application layer.
 */
@Getter
public class CreateCommunityRequest {

    @NotBlank
    private String name;

    private String description;

    /**
     * Maps request payload into a new Community aggregate.
     *
     * IMPORTANT:
     * - tenantId MUST come from the request context (multi-tenant boundary)
     * - createdBy MUST come from the request context (authenticated principal)
     */
    public Community toDomain(IdValue tenantId, IdValue createdBy) {
        return CommunityFactory.create(
                tenantId,
                name,
                description,
                createdBy
        );
    }
}