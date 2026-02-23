package com.dekra.service.regulations.standards.relationships.domain.model;

import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.domaincore.value.TimestampValue;
import com.dekra.service.regulations.standards.relationships.domain.value.StandardRelationshipPurposeValue;
import com.dekra.service.regulations.standards.relationships.domain.value.StandardRelationshipTypeValue;
import com.dekra.service.regulations.standards.relationships.domain.value.StandardVersionRefValue;

import java.util.Objects;

public final class StandardRelationship {

    private final IdValue id;

    // weak refs (no navigation)
    private final StandardVersionRefValue from;
    private final StandardVersionRefValue to;

    private final StandardRelationshipTypeValue type;
    private final StandardRelationshipPurposeValue purpose; // nullable depending on type

    private final TimestampValue createdAt;

    // package-private: enforce creation via factory
    StandardRelationship(IdValue id,
                         StandardVersionRefValue from,
                         StandardVersionRefValue to,
                         StandardRelationshipTypeValue type,
                         StandardRelationshipPurposeValue purpose,
                         TimestampValue createdAt) {

        this.id = Objects.requireNonNull(id, "id");
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        this.type = Objects.requireNonNull(type, "type");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");

        // purpose may be null; validated below
        this.purpose = purpose;

        validateInvariants();
    }

    // -------------------------
    // Getters
    // -------------------------

    public IdValue id() { return id; }

    public StandardVersionRefValue from() { return from; }
    public StandardVersionRefValue to() { return to; }

    public StandardRelationshipTypeValue type() { return type; }
    public StandardRelationshipPurposeValue purpose() { return purpose; }

    public TimestampValue createdAt() { return createdAt; }

    // -------------------------
    // Invariants
    // -------------------------

    private void validateInvariants() {

        // INV-R1: no self relationship
        if (from.equals(to)) {
            throw new IllegalArgumentException("StandardRelationship cannot reference itself (from == to)");
        }

        // INV-R4: purpose only allowed/required for REFERS_TO
        if (type.isRefersTo()) {
            if (purpose == null) {
                throw new IllegalArgumentException("purpose is required for REFERS_TO relationships");
            }
        } else {
            if (purpose != null) {
                throw new IllegalArgumentException("purpose must be null unless type is REFERS_TO");
            }
        }

        // INV-R5: normalize EQUIVALENT_TO (canonical direction)
        if (type.isEquivalentTo() && from.compareTo(to) > 0) {
            throw new IllegalArgumentException("EQUIVALENT_TO must be stored using canonical (from <= to) ordering");
        }
    }

    /**
     * Domain-level uniqueness key used by repository/indexes.
     * (Not an invariant by itself; uniqueness enforced by persistence / repository.)
     */
    public String uniquenessKey() {
        return from.key() + "|" + type.getCode() + "|" + (purpose != null ? purpose.getCode() : "-") + "|" + to.key();
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) || (o instanceof StandardRelationship other && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "StandardRelationship{" +
                "id=" + id +
                ", from=" + from +
                ", to=" + to +
                ", type=" + type +
                ", purpose=" + purpose +
                '}';
    }



}
