package com.paravai.communities.resource.domain.model;

import com.paravai.communities.resource.domain.value.ResourceConditionValue;
import com.paravai.communities.resource.domain.value.ResourceTitleValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.TimestampValue;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root: Resource
 *
 * Represents a resource owned by a user that can later be offered
 * inside a community.
 *
 * MVP scope:
 * - basic registration only
 * - no photos
 * - no advanced categories
 * - no inventory
 * - no ownership verification
 *
 * Aggregate-level invariants:
 * - every Resource must have a stable identity
 * - every Resource must have an ownerId
 * - ownerId is immutable once the resource is created
 * - title is mandatory
 * - description is optional
 * - condition is optional
 * - createdAt cannot be after updatedAt
 *
 * Repository / application-level invariants (NOT enforceable from a single aggregate instance):
 * - none required for MVP C1 beyond persistence
 */
public final class Resource implements Serializable {

    private final IdValue id;
    private final IdValue tenantId;
    private final IdValue ownerId;

    /**
     * Mandatory business title of the resource.
     */
    private ResourceTitleValue title;

    /**
     * Optional free-text description.
     *
     * By convention, for optional informative fields that do not add
     * strong domain behavior, we keep them as plain String in MVP.
     */
    private String description;

    /**
     * Optional simple condition catalog value.
     */
    private ResourceConditionValue condition;

    private final TimestampValue createdAt;
    private TimestampValue updatedAt;

    /**
     * Constructor intended for ResourceFactory.create(...) / recreate(...).
     */
    Resource(IdValue id,
             IdValue tenantId,
             IdValue ownerId,
             ResourceTitleValue title,
             String description,
             ResourceConditionValue condition,
             TimestampValue createdAt,
             TimestampValue updatedAt,
             boolean validate) {

        this.id = Objects.requireNonNull(id, "Resource id is required");
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId is required");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId is required");

        this.title = Objects.requireNonNull(title, "title is required");
        this.description = normalizeDescription(description);
        this.condition = condition;

        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");

        if (validate) {
            validateInvariants(Clock.systemUTC());
        }
    }

    /**
     * Updates basic editable information of the resource.
     *
     * Covered invariants:
     * - title is mandatory
     * - description remains normalized
     * - updatedAt must remain coherent
     *
     * Idempotency:
     * - if nothing changes, returns false
     */
    public boolean updateDetails(ResourceTitleValue newTitle,
                                 String newDescription,
                                 ResourceConditionValue newCondition,
                                 TimestampValue when) {

        Objects.requireNonNull(newTitle, "newTitle is required");

        String normalizedDescription = normalizeDescription(newDescription);

        boolean sameTitle = this.title.equals(newTitle);
        boolean sameDescription = Objects.equals(this.description, normalizedDescription);
        boolean sameCondition = Objects.equals(this.condition, newCondition);

        if (sameTitle && sameDescription && sameCondition) {
            return false;
        }

        TimestampValue effectiveWhen = (when != null ? when : TimestampValue.now());

        this.title = newTitle;
        this.description = normalizedDescription;
        this.condition = newCondition;
        touch(effectiveWhen);

        validateInvariants(Clock.systemUTC());
        return true;
    }

    /**
     * Convenience method for ownership checks.
     */
    public boolean isOwnedBy(IdValue userId) {
        return ownerId.equals(userId);
    }

    private void validateInvariants(Clock clock) {
        Instant now = clock.instant();

        if (createdAt.isAfter(updatedAt)) {
            throw new IllegalStateException("createdAt cannot be after updatedAt");
        }

        if (createdAt.getInstant().isAfter(now)) {
            throw new IllegalArgumentException("createdAt cannot be in the future");
        }

        if (updatedAt.getInstant().isAfter(now)) {
            throw new IllegalArgumentException("updatedAt cannot be in the future");
        }
    }

    private void touch(TimestampValue when) {
        this.updatedAt = (when != null ? when : TimestampValue.now());
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        String normalized = description.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public IdValue id() {
        return id;
    }

    public IdValue tenantId() {
        return tenantId;
    }

    public IdValue ownerId() {
        return ownerId;
    }

    public ResourceTitleValue title() {
        return title;
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Optional<ResourceConditionValue> condition() {
        return Optional.ofNullable(condition);
    }

    public TimestampValue createdAt() {
        return createdAt;
    }

    public TimestampValue updatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Resource{id=%s, tenantId=%s, ownerId=%s, title=%s, condition=%s}"
                .formatted(
                        id.value(),
                        tenantId.value(),
                        ownerId.value(),
                        title.value(),
                        condition != null ? condition.value() : "null"
                );
    }
}