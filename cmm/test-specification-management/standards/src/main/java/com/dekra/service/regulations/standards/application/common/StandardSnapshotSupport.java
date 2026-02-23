package com.paravai.regulations.standards.application.common;

import com.paravai.foundation.snapshot.SnapshotMapper;
import com.paravai.regulations.standards.domain.model.Standard;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public final class StandardSnapshotSupport {

    private final SnapshotMapper<Standard> snapshotMapper;

    public StandardSnapshotSupport(SnapshotMapper<Standard> snapshotMapper) {
        this.snapshotMapper = Objects.requireNonNull(snapshotMapper, "snapshotMapper");
    }

    public JsonNode snapshot(Standard standard) {
        return snapshotMapper.toSnapshot(standard);
    }
}
