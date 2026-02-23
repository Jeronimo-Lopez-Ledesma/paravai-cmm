package com.dekra.service.foundation.integration.domain.event;

import java.util.Locale;
import java.util.Objects;

public final class SchemaId {

    private SchemaId() {}

    /**
     * Canonical schemaId:
     *   <cmm>.<aggregate>.<channel>.v<major>
     *
     * Examples:
     *   tspecm.standard.audit.v1
     *   certm.certificate.historization.v2
     */
    public static String of(EventCmm cmm, String aggregate, EventChannel channel, int major) {
        Objects.requireNonNull(cmm, "cmm");
        requireToken(aggregate, "component");
        Objects.requireNonNull(channel, "channel");
        if (major < 1) throw new IllegalArgumentException("schema major must be >= 1");

        return (cmm.token() + "." + aggregate + "." + channel.token() + ".v" + major)
                .toLowerCase(Locale.ROOT);
    }

    private static void requireToken(String v, String name) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(name + " cannot be blank");
        // keep aligned with your previous constraint: no dashes/underscores
        if (!v.matches("[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException(name + " must be alphanumeric (no dashes/underscores): " + v);
        }
    }
}
