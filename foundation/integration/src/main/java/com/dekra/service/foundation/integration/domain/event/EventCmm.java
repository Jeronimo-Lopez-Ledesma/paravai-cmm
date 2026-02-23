package com.dekra.service.foundation.integration.domain.event;

import java.util.Locale;

public enum EventCmm {

    TESTSPEC("test-specification-management"),          // Test Specification Management
    CERT("certificate-management"),                     // Certificate Management
    PRESALESTA("pre-sales-technical-analysis"),         // Presales Technical Analysis
    CUSTPRODUCT("customer-product-management");         // Custom Product Management

    private final String token;

    EventCmm(String token) {
        this.token = token.toLowerCase(Locale.ROOT);
    }

    public String token() {
        return token;
    }
}
