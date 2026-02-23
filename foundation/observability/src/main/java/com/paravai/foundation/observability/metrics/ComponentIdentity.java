package com.paravai.foundation.observability.metrics;

import java.util.Objects;

public record ComponentIdentity(
        String cmm,
        String module,
        String moduleType,
        String component
) {
    public ComponentIdentity {
        Objects.requireNonNull(cmm, "cmm");
        Objects.requireNonNull(module, "module");
        Objects.requireNonNull(moduleType, "moduleType");
        Objects.requireNonNull(component, "component");
    }

    public OperationCtx app(String operation) {
        return OperationCtx.application(cmm, module, moduleType, component, operation);
    }

    public OperationCtx outbound(String adapter, String operation) {
        return OperationCtx.outbound(cmm, module, moduleType, component, adapter, operation);
    }
}
