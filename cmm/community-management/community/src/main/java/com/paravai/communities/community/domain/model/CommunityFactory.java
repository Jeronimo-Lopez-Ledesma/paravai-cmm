package com.paravai.communities.community.domain.model;

import com.paravai.communities.community.domain.value.CommunityRulesValue;
import com.paravai.communities.community.domain.value.CommunityStatusValue;
import com.paravai.communities.community.domain.value.CommunityVisibilityValue;
import com.paravai.foundation.domain.value.IdValue;

import java.time.Instant;
import java.util.Objects;

/**
 * Factory: CommunityFactory
 *
 * Encapsulates valid Community creation and reconstruction.
 *
 * Methods:
 * - create(): for new communities (A1)
 * - recreate(): for rehydration from persistence
 */
public final class CommunityFactory {

    private CommunityFactory() {
        throw new IllegalStateException("Factory class — not instantiable");
    }

    // -------------------------------------------------
    // Creation
    // -------------------------------------------------

    /**
     * Creates a new Community (A1).
     *
     * Defaults:
     * - visibility = PRIVATE
     * - status = ACTIVE
     * - rules = null
     *
     * @param tenantId    Tenant identifier
     * @param name        Community name (required)
     * @param description Optional description (informational)
     * @param createdBy   User id who creates the community
     */
    public static Community create(IdValue tenantId,
                                   String name,
                                   String description,
                                   IdValue createdBy) {

        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(createdBy, "createdBy is required");

        String normalizedName = Community.requireNonBlank(name, "Community name is required");
        String slug = Community.SlugUtil.toSlug(normalizedName);

        Instant now = Instant.now();

        return new Community(
                IdValue.generate(),
                tenantId,
                normalizedName,
                slug,
                description,
                CommunityVisibilityValue.of("PRIVATE"),
                null,
                CommunityStatusValue.of("ACTIVE"),
                null,
                createdBy,
                now,
                now,
                true
        );
    }

    /**
     * Overload if you ever want to control visibility at creation (not MVP default).
     */
    public static Community create(IdValue tenantId,
                                   String name,
                                   String description,
                                   IdValue createdBy,
                                   CommunityVisibilityValue visibility) {

        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(createdBy, "createdBy is required");
        Objects.requireNonNull(visibility, "visibility is required");

        String normalizedName = Community.requireNonBlank(name, "Community name is required");
        String slug = Community.SlugUtil.toSlug(normalizedName);

        Instant now = Instant.now();

        return new Community(
                IdValue.generate(),
                tenantId,
                normalizedName,
                slug,
                description,
                visibility,
                null,
                CommunityStatusValue.of("ACTIVE"),
                null,
                createdBy,
                now,
                now,
                true
        );
    }

    // -------------------------------------------------
    // Reconstruction (rehydration)
    // -------------------------------------------------

    /**
     * Recreates an existing Community from persistence.
     * No extra validations beyond basic null checks (assumes data consistency).
     */
    public static Community recreate(IdValue id,
                                     IdValue tenantId,
                                     String name,
                                     String slug,
                                     String description,
                                     CommunityVisibilityValue visibility,
                                     CommunityRulesValue rules,
                                     CommunityStatusValue status,
                                     Instant archivedAt,
                                     IdValue createdBy,
                                     Instant createdAt,
                                     Instant updatedAt) {

        Objects.requireNonNull(id, "Community id is required");
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(slug, "slug is required");
        Objects.requireNonNull(visibility, "visibility is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(createdBy, "createdBy is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");

        return new Community(
                id,
                tenantId,
                name,
                slug,
                description,
                visibility,
                rules,
                status,
                archivedAt,
                createdBy,
                createdAt,
                updatedAt,
                false // no domain validation on recreate
        );
    }
}