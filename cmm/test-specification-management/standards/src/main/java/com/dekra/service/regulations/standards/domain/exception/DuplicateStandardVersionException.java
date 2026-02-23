package com.dekra.service.regulations.standards.domain.exception;

import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.regulations.standards.domain.value.StandardTypeValue;
import com.dekra.service.regulations.standards.domain.value.StandardVersionValue;

import java.util.Objects;

public final class DuplicateStandardVersionException extends RuntimeException {

    private final IdValue standardId;
    private final StandardVersionValue version;

    public DuplicateStandardVersionException(IdValue standardId,
                                             StandardVersionValue version
                                             ) {
        super("Duplicate standard version for standardId=%s, version=%s, type=%s"
                .formatted(standardId, version));
        this.standardId = Objects.requireNonNull(standardId, "standardId");
        this.version = Objects.requireNonNull(version, "version");

    }

    public IdValue standardId() {
        return standardId;
    }

    public StandardVersionValue version() {
        return version;
    }


}
