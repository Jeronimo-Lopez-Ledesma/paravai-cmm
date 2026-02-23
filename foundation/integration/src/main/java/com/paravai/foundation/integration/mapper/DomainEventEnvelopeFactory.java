package com.paravai.foundation.integration.mapper;

import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import com.paravai.foundation.integration.domain.event.EventChannel;
import com.paravai.foundation.integration.domain.event.EventCmm;
import com.paravai.foundation.integration.domain.event.SchemaId;

import java.util.Objects;

public final class DomainEventEnvelopeFactory {

    private DomainEventEnvelopeFactory() {}

    // Canonical: schemaId already known
    public static <T> DomainEventEnvelope<T> create(EntityChangedEvent event, String schemaId, T payload) {
        return createInternal(event, schemaId, null, payload);
    }

    // Canonical: schemaId from typed parts (recommended)
    public static <T> DomainEventEnvelope<T> create(EntityChangedEvent event,
                                                    EventCmm cmm,
                                                    String component,
                                                    EventChannel channel,
                                                    int major,
                                                    T payload) {
        Objects.requireNonNull(cmm, "cmm");
        Objects.requireNonNull(channel, "channel");
        String schemaId = SchemaId.of(cmm, component, channel, major);
        return create(event, schemaId, payload);
    }

    // Legacy-only: explicitly sets legacy "version"
    @Deprecated
    public static <T> DomainEventEnvelope<T> createLegacy(EntityChangedEvent event,
                                                          String schemaId,
                                                          String legacyVersion,
                                                          T payload) {
        return createInternal(event, schemaId, legacyVersion, payload);
    }

    @Deprecated
    public static <T> DomainEventEnvelope<T> createLegacy(EntityChangedEvent event,
                                                          EventCmm cmm,
                                                          String component,
                                                          EventChannel channel,
                                                          int major,
                                                          String legacyVersion,
                                                          T payload) {
        Objects.requireNonNull(cmm, "cmm");
        Objects.requireNonNull(channel, "channel");
        String schemaId = SchemaId.of(cmm, component, channel, major);
        return createLegacy(event, schemaId, legacyVersion, payload);
    }

    private static <T> DomainEventEnvelope<T> createInternal(EntityChangedEvent event,
                                                             String schemaId,
                                                             String legacyVersion,
                                                             T payload) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(schemaId, "schemaId");
        Objects.requireNonNull(payload, "payload");

        return new DomainEventEnvelope<>(
                event.metadata().eventId().toString(),
                event.getEntityId().toString(),
                event.getEntityType().toString(),
                event.getOperationType().toString(),
                event.getSourceService(),
                event.metadata().occurredOn().getInstant(),
                event.getTraceId() != null ? event.getTraceId().toString() : null,
                event.getUserOid() != null ? event.getUserOid().toString() : null,
                schemaId,
                legacyVersion,
                payload
        );
    }
}
