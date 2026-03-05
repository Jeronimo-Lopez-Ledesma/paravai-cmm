package com.paravai.communities.membership.infrastructure.event.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.membership.infrastructure.event.MembershipEventPayloadV1;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import com.paravai.foundation.integration.domain.event.EventChannel;
import com.paravai.foundation.integration.domain.event.EventCmm;
import com.paravai.foundation.integration.domain.event.SchemaId;
import com.paravai.foundation.integration.mapper.DomainEventEnvelopeFactory;
import org.springframework.stereotype.Component;

@Component
public class MembershipEventEnvelopeMapper {

    private static final EventCmm CMM = EventCmm.COMMUNITIES_MANAGEMENT;
    private static final String COMPONENT = "membership";
    private static final int MAJOR = 1;

    public DomainEventEnvelope<MembershipEventPayloadV1> map(EntityChangedEvent e) {

        JsonNode snapshot = e.getCurrentState() != null
                ? e.getCurrentState()
                : e.getPreviousState();

        if (snapshot == null || snapshot.isNull()) {
            throw new IllegalStateException("Cannot build CommunityEventPayloadV1: snapshot is null");
        }

        MembershipEventPayloadV1 payload = MembershipEventPayloadV1.fromSnapshot(snapshot);

        String schemaId = SchemaId.of(CMM, COMPONENT, EventChannel.INTEGRATION, MAJOR);
        return DomainEventEnvelopeFactory.create(e, schemaId, payload);
    }
}
