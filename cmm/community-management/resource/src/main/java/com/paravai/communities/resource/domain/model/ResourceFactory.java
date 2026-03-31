package com.paravai.communities.resource.domain.model;

import com.paravai.communities.resource.domain.value.ResourceConditionValue;
import com.paravai.communities.resource.domain.value.ResourceTitleValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.TimestampValue;

import java.util.Objects;

/**
 * Factory: ResourceFactory
 *
 * Encapsulates valid Resource creation and reconstruction.
 *
 * Methods:
 * - create(): creates a new Resource owned by a user
 * - recreate(): rehydrates an existing Resource from persistence
 */
public final class ResourceFactory {

    private ResourceFactory() {
        throw new IllegalStateException("Factory class — not instantiable");
    }

    // -------------------------------------------------
    // Creation
    // -------------------------------------------------

    /**
     * Creates a new Resource owned by a user.
     *
     * Covered invariants:
     * - resource must have id
     * - resource must have tenantId
     * - resource must have ownerId
     * - title is mandatory
     * - description is normalized
     * - createdAt / updatedAt are initialized consistently
     */
    public static Resource create(IdValue tenantId,
                                  IdValue ownerId,
                                  ResourceTitleValue title,
                                  String description,
                                  ResourceConditionValue condition) {

        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");
        Objects.requireNonNull(title, "title is required");

        TimestampValue now = TimestampValue.now();

        return new Resource(
                IdValue.generate(),
                tenantId,
                ownerId,
                title,
                description,
                condition,
                now,
                now,
                true
        );
    }

    // -------------------------------------------------
    // Reconstruction (rehydration)
    // -------------------------------------------------

    /**
     * Recreates an existing Resource from persistence.
     *
     * Assumes the persisted state was already validated when written.
     * This method performs only structural mandatory checks.
     */
    public static Resource recreate(IdValue id,
                                    IdValue tenantId,
                                    IdValue ownerId,
                                    ResourceTitleValue title,
                                    String description,
                                    ResourceConditionValue condition,
                                    TimestampValue createdAt,
                                    TimestampValue updatedAt) {

        Objects.requireNonNull(id, "Resource id is required");
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");
        Objects.requireNonNull(title, "title is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");

        return new Resource(
                id,
                tenantId,
                ownerId,
                title,
                description,
                condition,
                createdAt,
                updatedAt,
                false
        );
    }
}