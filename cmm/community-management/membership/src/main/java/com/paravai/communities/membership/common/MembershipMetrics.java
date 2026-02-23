package com.paravai.communities.membership.common;

import com.paravai.foundation.observability.metrics.ComponentIdentity;
import com.paravai.foundation.observability.metrics.ModuleType;

public final class MembershipMetrics {

    private MembershipMetrics() {}

    public static final ComponentIdentity ID = new ComponentIdentity(
            "communities",                 // CMM (or repo/cmm identifier)
            "membership",                   // module
            ModuleType.AGGREGATE.value(),  // moduleType
            "Membership"                     // component
    );
}
