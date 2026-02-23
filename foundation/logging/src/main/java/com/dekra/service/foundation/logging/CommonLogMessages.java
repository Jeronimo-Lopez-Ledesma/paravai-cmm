package com.dekra.service.foundation.logging;

public final class CommonLogMessages {

    private CommonLogMessages() {
        throw new IllegalStateException("Utility class");
    }

    // General
    public static final String REQUEST_RECEIVED = "Received {} request to {}";
    public static final String OPERATION_SUCCESS = "Operation completed successfully";
    public static final String OPERATION_FAILED = "Operation failed";
    public static final String TRACE_USER_CONTEXT = "TraceId={}, UserOid={}";

    // Create
    public static final String CREATING_ENTITY = "Creating new {}";
    public static final String CREATED_ENTITY = "New {} created with ID {}";
    public static final String CREATE_ENTITY_FAILED = "Failed to create {}";

    // Find by ID
    public static final String FINDING_ENTITY_BY_ID = "Finding {} with ID {}";
    public static final String ENTITY_NOT_FOUND = "{} with ID {} not found";

    // Find by Filter
    public static final String FINDING_ENTITIES_WITH_FILTERS = "Finding {} entities with filters: {}";

    // Update
    public static final String UPDATING_ENTITY = "Updating {} with ID {}";
    public static final String UPDATED_ENTITY = "{} updated with ID {}";
    public static final String UPDATE_ENTITY_FAILED = "Failed to update {} with ID {}";

    // Delete
    public static final String DELETING_ENTITY = "Deleting {} with ID {}";
    public static final String DELETED_ENTITY = "{} deleted with ID {}";
    public static final String DELETE_ENTITY_FAILED = "Failed to delete {} with ID {}";

    // Validation and business errors
    public static final String VALIDATION_FAILED = "Validation failed for {}: {}";
    public static final String BUSINESS_RULE_VIOLATION = "Business rule violated while processing {}: {}";

}