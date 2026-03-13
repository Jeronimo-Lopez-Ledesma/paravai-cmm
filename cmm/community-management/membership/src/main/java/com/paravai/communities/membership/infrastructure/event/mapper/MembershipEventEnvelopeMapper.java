package com.paravai.communities.membership.infrastructure.event.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.contracts.event.membership.MembershipEventPayloadV1;
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

    private final MembershipSnapshotToEventPayloadMapperV1 snapshotMapper;

    public MembershipEventEnvelopeMapper(MembershipSnapshotToEventPayloadMapperV1 snapshotMapper) {
        this.snapshotMapper = snapshotMapper;
    }

    public DomainEventEnvelope<MembershipEventPayloadV1> map(EntityChangedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("EntityChangedEvent must not be null");
        }

        JsonNode snapshot = event.getCurrentState() != null
                ? event.getCurrentState()
                : event.getPreviousState();

        if (snapshot == null || snapshot.isNull()) {
            throw new IllegalStateException("Cannot build MembershipEventPayloadV1: snapshot is null");
        }

        MembershipEventPayloadV1 payload = snapshotMapper.map(snapshot);

        String schemaId = SchemaId.of(CMM, COMPONENT, EventChannel.INTEGRATION, MAJOR);
        return DomainEventEnvelopeFactory.create(event, schemaId, payload);
    }
}