package com.paravai.foundation.snapshot;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Contract for generating a serializable snapshot of an aggregate.
 */
public interface SnapshotMapper<T> {
    JsonNode toSnapshot(T aggregate);
}