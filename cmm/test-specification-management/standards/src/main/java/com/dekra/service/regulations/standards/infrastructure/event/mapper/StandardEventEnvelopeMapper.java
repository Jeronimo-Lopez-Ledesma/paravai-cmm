package com.paravai.regulations.standards.infrastructure.event.mapper;

import com.paravai.foundation.domaincore.event.EntityChangedEvent;
import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import com.paravai.foundation.integration.domain.event.EventChannel;
import com.paravai.foundation.integration.domain.event.EventCmm;
import com.paravai.foundation.integration.domain.event.SchemaId;
import com.paravai.foundation.integration.mapper.DomainEventEnvelopeFactory;
import com.paravai.regulations.standards.infrastructure.event.StandardEventPayloadV1;
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
