package com.paravai.foundation.integration.domain.event;

import java.util.Locale;

public enum EventCmm {

    COMMUNITIES_MANAGEMENT("communities-management"),          // Communities Management
    CERT("certificate-management"),              // Certificate Management
    PRESALESTA("pre-sales-technical-analysis"),  // Presales Technical Analysis
    CUSTPRODUCT("customer-product-management");  // Custom Product Management

    private final String token;

    EventCmm(String token) {
        this.token = token.toLowerCase(Locale.ROOT);
    }

    public String token() {
        return token;
    }
}
