package com.paravai.regulations.standards.application.common;

import com.paravai.foundation.observability.metrics.ComponentIdentity;
import com.paravai.foundation.observability.metrics.ModuleType;

public final class StandardMetrics {

    private StandardMetrics() {}

    public static final ComponentIdentity ID = new ComponentIdentity(
            "regulations",                 // CMM (or repo/cmm identifier)
            "standards",                   // module
            ModuleType.AGGREGATE.value(),  // moduleType
            "Standard"                     // component
    );
}
