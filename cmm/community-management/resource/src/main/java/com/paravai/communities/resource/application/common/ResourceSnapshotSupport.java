package com.paravai.communities.resource.application.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.resource.domain.model.Resource;
import com.paravai.foundation.snapshot.SnapshotMapper;

import java.util.Objects;

public final class ResourceSnapshotSupport {

    private final SnapshotMapper<Resource> snapshotMapper;

    public ResourceSnapshotSupport(SnapshotMapper<Resource> snapshotMapper) {
        this.snapshotMapper = Objects.requireNonNull(snapshotMapper, "snapshotMapper");
    }

    public JsonNode snapshot(Resource resource) {
        return snapshotMapper.toSnapshot(resource);
    }
}