package com.paravai.communities.resource.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paravai.communities.resource.domain.model.Resource;
import com.paravai.communities.resource.infrastructure.persistence.mongo.event.mapper.ResourceToEventPayloadMapperV1;
import com.paravai.foundation.snapshot.SnapshotMapper;
import org.springframework.stereotype.Component;

/**
 * Serializes Resource aggregates into JSON snapshots for historization.
 */
@Component
public class ResourceSnapshotMapper implements SnapshotMapper<Resource> {

    private final ObjectMapper objectMapper;
    private final ResourceToEventPayloadMapperV1 payloadMapper;

    public ResourceSnapshotMapper(
            ObjectMapper objectMapper,
            ResourceToEventPayloadMapperV1 payloadMapper
    ) {
        this.objectMapper = objectMapper;
        this.payloadMapper = payloadMapper;
    }

    @Override
    public JsonNode toSnapshot(Resource aggregate) {
        return objectMapper.valueToTree(payloadMapper.map(aggregate));
    }
}