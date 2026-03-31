package com.paravai.communities.offer.application.snapshot;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.offer.domain.model.Offer;
import com.paravai.foundation.snapshot.SnapshotMapper;

import java.util.Objects;

public final class OfferSnapshotSupport {

    private final SnapshotMapper<Offer> snapshotMapper;

    public OfferSnapshotSupport(SnapshotMapper<Offer> snapshotMapper) {
        this.snapshotMapper = Objects.requireNonNull(snapshotMapper, "snapshotMapper");
    }

    public JsonNode snapshot(Offer membership) {
        return snapshotMapper.toSnapshot(membership);
    }
}
