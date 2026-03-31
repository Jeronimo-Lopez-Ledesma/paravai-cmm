package com.paravai.communities.resource.infrastructure.persistence.mongo.event.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.contracts.event.resource.ResourceEventPayloadV1;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import com.paravai.foundation.integration.domain.event.EventChannel;
import com.paravai.foundation.integration.domain.event.EventCmm;
import com.paravai.foundation.integration.domain.event.SchemaId;
import com.paravai.foundation.integration.mapper.DomainEventEnvelopeFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourceEventEnvelopeMapper {

    private static final EventCmm CMM = EventCmm.COMMUNITIES_MANAGEMENT;
    private static final String COMPONENT = "resource";
    private static final int MAJOR = 1;

    private final ResourceSnapshotToEventPayloadMapperV1 snapshotMapper;

    public ResourceEventEnvelopeMapper(ResourceSnapshotToEventPayloadMapperV1 snapshotMapper) {
        this.snapshotMapper = snapshotMapper;
    }

    public DomainEventEnvelope<ResourceEventPayloadV1> map(EntityChangedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("EntityChangedEvent must not be null");
        }

        JsonNode snapshot = event.getCurrentState() != null
                ? event.getCurrentState()
                : event.getPreviousState();

        if (snapshot == null || snapshot.isNull()) {
            throw new IllegalStateException("Cannot build ResourceEventPayloadV1: snapshot is null");
        }

        ResourceEventPayloadV1 payload = snapshotMapper.map(snapshot);

        String schemaId = SchemaId.of(CMM, COMPONENT, EventChannel.INTEGRATION, MAJOR);
        return DomainEventEnvelopeFactory.create(event, schemaId, payload);
    }
}