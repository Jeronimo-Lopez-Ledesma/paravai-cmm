package com.dekra.service.regulations.standards.relationships.domain.model;

import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.domaincore.value.TimestampValue;
import com.dekra.service.regulations.standards.relationships.domain.value.StandardRelationshipPurposeValue;
import com.dekra.service.regulations.standards.relationships.domain.value.StandardRelationshipTypeValue;
import com.dekra.service.regulations.standards.relationships.domain.value.StandardVersionRefValue;

import java.util.Objects;

public final class StandardRelationshipFactory {

    private StandardRelationshipFactory() {
        throw new IllegalStateException("Factory class");
    }

    public static StandardRelationship create(StandardVersionRefValue from,
                                              StandardVersionRefValue to,
                                              StandardRelationshipTypeValue type,
                                              StandardRelationshipPurposeValue purpose,
                                              TimestampValue now) {

        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(now, "now");

        if (type.isRefersTo() && purpose == null) {
            throw new IllegalArgumentException("purpose is required for REFERS_TO relationships");
        }
        if (!type.isRefersTo() && purpose != null) {
            throw new IllegalArgumentException("purpose must be null unless type is REFERS_TO");
        }

        // Canonical ordering for symmetric relationships
        if (type.isEquivalentTo() && from.compareTo(to) > 0) {
            StandardVersionRefValue tmp = from;
            from = to;
            to = tmp;
        }

        return new StandardRelationship(
                IdValue.generate(),
                from,
                to,
                type,
                purpose,
                now
        );
    }

    public static StandardRelationship recreate(IdValue id,
                                                StandardVersionRefValue from,
                                                StandardVersionRefValue to,
                                                StandardRelationshipTypeValue type,
                                                StandardRelationshipPurposeValue purpose,
                                                TimestampValue createdAt) {

        return new StandardRelationship(
                id,
                from,
                to,
                type,
                purpose,
                createdAt
        );
    }
}
