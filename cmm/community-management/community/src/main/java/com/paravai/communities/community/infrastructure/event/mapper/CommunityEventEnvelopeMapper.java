package com.paravai.communities.community.infrastructure.event.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.community.infrastructure.event.CommunityEventPayloadV1;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import com.paravai.foundation.integration.domain.event.EventChannel;
import com.paravai.foundation.integration.domain.event.EventCmm;
import com.paravai.foundation.integration.domain.event.SchemaId;
import com.paravai.foundation.integration.mapper.DomainEventEnvelopeFactory;

import org.springframework.stereotype.Component;

@Component
public class CommunityEventEnvelopeMapper {

    private static final EventCmm CMM = EventCmm.COMMUNITIES_MANAGEMENT;
    private static final String COMPONENT = "community";
    private static final int MAJOR = 1;

    public DomainEventEnvelope<CommunityEventPayloadV1> map(EntityChangedEvent e) {

        JsonNode snapshot = e.getCurrentState() != null
                ? e.getCurrentState()
                : e.getPreviousState();

        if (snapshot == null || snapshot.isNull()) {
            throw new IllegalStateException("Cannot build CommunityEventPayloadV1: snapshot is null");
        }

        CommunityEventPayloadV1 payload = CommunityEventPayloadV1.fromSnapshot(snapshot);

        String schemaId = SchemaId.of(CMM, COMPONENT, EventChannel.INTEGRATION, MAJOR);
        return DomainEventEnvelopeFactory.create(e, schemaId, payload);
    }
}
