package com.paravai.communities.offer.application.common;

import com.paravai.foundation.observability.metrics.ComponentIdentity;
import com.paravai.foundation.observability.metrics.ModuleType;

public final class OfferMetrics {

    private OfferMetrics() {}

    public static final ComponentIdentity ID = new ComponentIdentity(
            "communities",                 // CMM (or repo/cmm identifier)
            "offer",                   // module
            ModuleType.AGGREGATE.value(),  // moduleType
            "Offer"                     // component
    );
}
