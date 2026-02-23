package com.dekra.service.foundation.governance.dto.v1;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class EntityChangeTraceRequest {

    @NotBlank
    private String entityId;

    private String resourceType;

    @NotBlank
    private String entityType;

    private String oid;

    private String traceId;

    @NotBlank
    private String sourceService;

    @NotNull
    private String operationType;

    private String message;

    private JsonNode previousState;

    public EntityChangeTraceRequest(String entityId,
                                    String resourceType,
                                    String entityType,
                                    String oid,
                                    String traceId,
                                    String sourceService,
                                    String operationType,
                                    String message,
                                    JsonNode previousState) {
        this.entityId = entityId;
        this.resourceType = resourceType;
        this.entityType = entityType;
        this.oid = oid;
        this.traceId = traceId;
        this.sourceService = sourceService;
        this.operationType = operationType;
        this.message = message;
        this.previousState = previousState;
    }
}
