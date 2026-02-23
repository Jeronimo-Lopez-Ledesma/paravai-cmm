package com.paravai.regulations.standards.application.exception;

import com.paravai.foundation.domaincore.value.IdValue;

public class StandardNotFoundException extends RuntimeException {

    private final String standardId;

    public StandardNotFoundException(IdValue id) {
        super("Standard not found: " + (id != null ? id.getValue() : "null"));
        this.standardId = id != null ? id.getValue() : null;
    }

    public String getStandardId() {
        return standardId;
    }
}
