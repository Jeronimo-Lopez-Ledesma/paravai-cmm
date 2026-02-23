package com.paravai.foundation.integration.domain.event;

import java.util.Locale;

public enum EventChannel {
    AUDIT,
    HISTORIZATION,
    INTEGRATION,
    SEMANTIC;

    public String token() {
        return name().toLowerCase(Locale.ROOT);
    }
}
