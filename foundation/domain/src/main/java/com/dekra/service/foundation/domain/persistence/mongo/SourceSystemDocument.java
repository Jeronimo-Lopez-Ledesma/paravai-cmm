package com.paravai.foundation.domaincore.persistence.mongo;

import com.paravai.foundation.domaincore.value.SourceSystemValue;

public record SourceSystemDocument(String sourceSystemId, String sourceId) {

    public static SourceSystemDocument fromDomain(SourceSystemValue value) {
        if (value == null) return null;
        return new SourceSystemDocument(
                value.getSourceSystemId().getValue(),
                value.getSourceId().getValue()
        );
    }

    public SourceSystemValue toDomain() {
        return SourceSystemValue.of(sourceSystemId, sourceId);
    }
}