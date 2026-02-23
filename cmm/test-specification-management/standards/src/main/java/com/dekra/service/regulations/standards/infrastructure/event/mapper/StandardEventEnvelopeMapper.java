package com.dekra.service.regulations.standards.infrastructure.event.mapper;

import com.dekra.service.foundation.domaincore.event.EntityChangedEvent;
import com.dekra.service.foundation.integration.domain.event.DomainEventEnvelope;
import com.dekra.service.foundation.integration.domain.event.EventChannel;
import com.dekra.service.foundation.integration.domain.event.EventCmm;
import com.dekra.service.foundation.integration.domain.event.SchemaId;
import com.dekra.service.foundation.integration.mapper.DomainEventEnvelopeFactory;
import com.dekra.service.regulations.standards.infrastructure.event.StandardEventPayloadV1;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class StandardEventEnvelopeMapper {

    private static final EventCmm CMM = EventCmm.TESTSPEC;
    private static final String COMPONENT = "standard";
    private static final int MAJOR = 1;

    public DomainEventEnvelope<StandardEventPayloadV1> map(EntityChangedEvent e) {

        JsonNode snapshot = e.getCurrentState() != null
                ? e.getCurrentState()
                : e.getPreviousState();

        if (snapshot == null || snapshot.isNull()) {
            throw new IllegalStateException("Cannot build StandardEventPayloadV1: snapshot is null");
        }

        StandardEventPayloadV1 payload = StandardEventPayloadV1.fromSnapshot(snapshot);

        String schemaId = SchemaId.of(CMM, COMPONENT, EventChannel.INTEGRATION, MAJOR);
        return DomainEventEnvelopeFactory.create(e, schemaId, payload);
    }
}
