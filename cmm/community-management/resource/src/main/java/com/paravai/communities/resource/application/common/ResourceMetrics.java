package com.paravai.communities.resource.application.common;

import com.paravai.foundation.observability.metrics.ComponentIdentity;
import com.paravai.foundation.observability.metrics.ModuleType;

public final class ResourceMetrics {

    private ResourceMetrics() {}

    public static final ComponentIdentity ID = new ComponentIdentity(
            "communities",                 // CMM (or repo/cmm identifier)
            "resource",                   // module
            ModuleType.AGGREGATE.value(),  // moduleType
            "Resource"                     // component
    );
}
