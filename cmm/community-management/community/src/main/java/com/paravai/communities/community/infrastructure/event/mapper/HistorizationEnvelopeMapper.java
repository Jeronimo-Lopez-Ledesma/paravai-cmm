package com.paravai.communities.community.infrastructure.event.mapper;

import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.governance.events.historization.v1.HistorizationPayloadV1;
import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import com.paravai.foundation.integration.domain.event.EventChannel;
import com.paravai.foundation.integration.domain.event.EventCmm;
import com.paravai.foundation.integration.domain.event.SchemaId;
import com.paravai.foundation.integration.mapper.DomainEventEnvelopeFactory;
import org.springframework.stereotype.Component;

@Component
public class HistorizationEnvelopeMapper {

    private static final EventCmm CMM = EventCmm.COMMUNITIES_MANAGEMENT;
    private static final String COMPONENT = "community";
    private static final int MAJOR = 1;

    public DomainEventEnvelope<HistorizationPayloadV1> map(EntityChangedEvent e) {
        String schemaId = SchemaId.of(CMM, COMPONENT, EventChannel.HISTORIZATION, MAJOR);
        return DomainEventEnvelopeFactory.create(e, schemaId, HistorizationPayloadV1.from(e));
    }
}
