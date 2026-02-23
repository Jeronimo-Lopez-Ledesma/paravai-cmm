package com.paravai.regulations.standards.infrastructure.persistence.mapper;

import com.paravai.foundation.snapshot.SnapshotMapper;
import com.paravai.regulations.standards.domain.model.Standard;
import com.paravai.regulations.standards.infrastructure.event.StandardEventPayloadV1;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Serializes S aggregates into JSON snapshots for historization.
 */
@Component
public class StandardSnapshotMapper implements SnapshotMapper<Standard> {

    private final ObjectMapper objectMapper;

    public StandardSnapshotMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode toSnapshot(Standard aggregate) {
        // Snapshot is a stable, serializable contract object (not the domain)
        return objectMapper.valueToTree(StandardEventPayloadV1.from(aggregate));
    }
}
