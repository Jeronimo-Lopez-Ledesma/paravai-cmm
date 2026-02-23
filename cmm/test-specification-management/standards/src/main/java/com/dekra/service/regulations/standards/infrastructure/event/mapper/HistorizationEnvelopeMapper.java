package com.dekra.service.regulations.standards.infrastructure.event.mapper;

import com.dekra.service.foundation.domaincore.event.EntityChangedEvent;
import com.dekra.service.foundation.governance.events.historization.v1.HistorizationPayloadV1;
import com.dekra.service.foundation.integration.domain.event.EventChannel;
import com.dekra.service.foundation.integration.domain.event.EventCmm;
import com.dekra.service.foundation.integration.domain.event.SchemaId;
import com.dekra.service.foundation.integration.domain.event.DomainEventEnvelope;
import com.dekra.service.foundation.integration.mapper.DomainEventEnvelopeFactory;
import org.springframework.stereotype.Component;

@Component
public class HistorizationEnvelopeMapper {

    private static final EventCmm CMM = EventCmm.TESTSPEC;
    private static final String COMPONENT = "standard";
    private static final int MAJOR = 1;

    public DomainEventEnvelope<HistorizationPayloadV1> map(EntityChangedEvent e) {
        String schemaId = SchemaId.of(CMM, COMPONENT, EventChannel.HISTORIZATION, MAJOR);
        return DomainEventEnvelopeFactory.create(e, schemaId, HistorizationPayloadV1.from(e));
    }
}
