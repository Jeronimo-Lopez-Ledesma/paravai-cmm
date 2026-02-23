package com.dekra.service.regulations.standards.relationships.infrastructure.persistence.mapper;

import com.dekra.service.foundation.snapshot.SnapshotMapper;
import com.dekra.service.regulations.standards.relationships.domain.model.StandardRelationship;
import com.dekra.service.regulations.standards.relationships.infrastructure.event.StandardRelationshipEventPayloadV1;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Serializes S aggregates into JSON snapshots
 */
@Component
public class StandardRelationshipSnapshotMapper implements SnapshotMapper<StandardRelationship> {

    private final ObjectMapper objectMapper;

    public StandardRelationshipSnapshotMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode toSnapshot(StandardRelationship aggregate) {
        // Snapshot is a stable, serializable contract object (not the domain)
        return objectMapper.valueToTree(StandardRelationshipEventPayloadV1.from(aggregate));
    }
}
