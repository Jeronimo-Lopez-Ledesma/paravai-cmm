package com.paravai.communities.community.application.common;

import com.paravai.foundation.observability.metrics.ComponentIdentity;
import com.paravai.foundation.observability.metrics.ModuleType;

public final class CommunityMetrics {

    private CommunityMetrics() {}

    public static final ComponentIdentity ID = new ComponentIdentity(
            "communities",                 // CMM (or repo/cmm identifier)
            "community",                   // module
            ModuleType.AGGREGATE.value(),  // moduleType
            "Community"                     // component
    );
}
