package com.dekra.service.regulations.standards.infrastructure.event.mapper;

import com.dekra.service.foundation.domaincore.event.EntityChangedEvent;
import com.dekra.service.foundation.governance.events.audit.v1.AuditTrailPayloadV1;
import com.dekra.service.foundation.integration.domain.event.EventChannel;
import com.dekra.service.foundation.integration.domain.event.EventCmm;
import com.dekra.service.foundation.integration.domain.event.SchemaId;
import com.dekra.service.foundation.integration.domain.event.DomainEventEnvelope;
import com.dekra.service.foundation.integration.mapper.DomainEventEnvelopeFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditTrailEnvelopeMapper {

    private static final EventCmm CMM = EventCmm.TESTSPEC;
    private static final String COMPONENT = "standard";
    private static final int MAJOR = 1;

    public DomainEventEnvelope<AuditTrailPayloadV1> map(EntityChangedEvent e) {
        String schemaId = SchemaId.of(CMM, COMPONENT, EventChannel.AUDIT, MAJOR);
        return DomainEventEnvelopeFactory.create(e, schemaId, AuditTrailPayloadV1.from(e));
    }
}
