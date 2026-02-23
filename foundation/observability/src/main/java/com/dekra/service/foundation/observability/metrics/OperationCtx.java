package com.paravai.foundation.observability.metrics;

import java.util.Map;
import java.util.Objects;

public record OperationCtx(
        String metricName,
        Map<String, String> tags
) {
    public OperationCtx {
        Objects.requireNonNull(metricName, "metricName");
        Objects.requireNonNull(tags, "tags");
    }

    public static OperationCtx application(String cmm,
                                           String module,
                                           String moduleType,
                                           String component,
                                           String operation) {
        return new OperationCtx(
                "cmm.application.operation.duration",
                Map.of(
                        "cmm", cmm,
                        "module", module,
                        "moduleType", moduleType,
                        "component", component,
                        "operation", operation
                )
        );
    }

    public static OperationCtx outbound(String cmm,
                                        String module,
                                        String moduleType,
                                        String component,
                                        String adapter,
                                        String operation) {
        return new OperationCtx(
                "cmm.outbound.operation.duration",
                Map.of(
                        "cmm", cmm,
                        "module", module,
                        "moduleType", moduleType,
                        "component", component,
                        "adapter", adapter,
                        "operation", operation
                )
        );
    }


}
