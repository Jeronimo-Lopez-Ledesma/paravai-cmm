package com.paravai.regulations.standards.relationships.application.common;

import com.paravai.foundation.snapshot.SnapshotMapper;
import com.paravai.regulations.standards.relationships.domain.model.StandardRelationship;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public final class StandardRelationshipSnapshotSupport {

    private final SnapshotMapper<StandardRelationship> snapshotMapper;

    public StandardRelationshipSnapshotSupport(SnapshotMapper<StandardRelationship> snapshotMapper) {
        this.snapshotMapper = Objects.requireNonNull(snapshotMapper, "snapshotMapper");
    }

    public JsonNode snapshot(StandardRelationship standard) {
        return snapshotMapper.toSnapshot(standard);
    }
}
