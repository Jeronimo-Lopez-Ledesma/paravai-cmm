package com.paravai.regulations.standards.relationships.domain.value;

import com.paravai.foundation.domaincore.value.IdValue;

import java.util.Objects;

public final class StandardVersionRefValue implements Comparable<StandardVersionRefValue> {

    private final IdValue standardId;
    private final IdValue versionId;

    private StandardVersionRefValue(IdValue standardId, IdValue versionId) {
        this.standardId = Objects.requireNonNull(standardId, "standardId");
        this.versionId = Objects.requireNonNull(versionId, "versionId");
    }

    public static StandardVersionRefValue of(IdValue standardId, IdValue versionId) {
        return new StandardVersionRefValue(standardId, versionId);
    }

    public IdValue standardId() { return standardId; }
    public IdValue versionId() { return versionId; }

    /** Stable key to support uniqueness / indexing at repo level. */
    public String key() {
        return standardId.getValue() + ":" + versionId.getValue();
    }

    @Override
    public int compareTo(StandardVersionRefValue other) {
        if (other == null) return 1;
        int c = this.standardId.getValue().compareTo(other.standardId.getValue());
        if (c != 0) return c;
        return this.versionId.getValue().compareTo(other.versionId.getValue());
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) || (o instanceof StandardVersionRefValue other
                && standardId.equals(other.standardId)
                && versionId.equals(other.versionId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(standardId, versionId);
    }

    @Override
    public String toString() {
        return key();
    }
}
