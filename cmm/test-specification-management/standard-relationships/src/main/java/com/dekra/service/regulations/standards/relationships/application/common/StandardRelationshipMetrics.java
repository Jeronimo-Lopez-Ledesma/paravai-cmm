package com.dekra.service.regulations.standards.relationships.application.common;

import com.dekra.service.foundation.observability.metrics.ComponentIdentity;
import com.dekra.service.foundation.observability.metrics.ModuleType;

public final class StandardRelationshipMetrics {

    private StandardRelationshipMetrics() {}

    public static final ComponentIdentity ID = new ComponentIdentity(
            "regulations",                 // CMM (or repo/cmm identifier)
            "standard-relationships",                   // module
            ModuleType.AGGREGATE.value(),  // moduleType
            "Standard Relationships"                     // component
    );
}
