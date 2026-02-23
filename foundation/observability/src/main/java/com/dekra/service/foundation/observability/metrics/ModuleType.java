package com.dekra.service.foundation.observability.metrics;

public enum ModuleType {
    AGGREGATE("aggregate"),
    READMODEL("readmodel"),
    COMPOSITION("composition"),
    ORCHESTRATOR("orchestrator");

    private final String value;

    ModuleType(String value) { this.value = value; }

    public String value() { return value; }
}
