package com.dekra.service.regulations.standards.application.common;

import com.dekra.service.foundation.observability.metrics.ComponentIdentity;
import com.dekra.service.foundation.observability.metrics.ModuleType;

public final class StandardMetrics {

    private StandardMetrics() {}

    public static final ComponentIdentity ID = new ComponentIdentity(
            "regulations",                 // CMM (or repo/cmm identifier)
            "standards",                   // module
            ModuleType.AGGREGATE.value(),  // moduleType
            "Standard"                     // component
    );
}
