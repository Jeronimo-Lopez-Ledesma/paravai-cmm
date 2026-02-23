package com.paravai.foundation.domain.persistence.mongo;

import com.paravai.foundation.domain.value.SourceSystemValue;

public record SourceSystemDocument(String sourceSystemId, String sourceId) {

    public static SourceSystemDocument fromDomain(SourceSystemValue value) {
        if (value == null) return null;
        return new SourceSystemDocument(
                value.getSourceSystemId().value(),
                value.getSourceId().value()
        );
    }

    public SourceSystemValue toDomain() {
        return SourceSystemValue.of(sourceSystemId, sourceId);
    }
}