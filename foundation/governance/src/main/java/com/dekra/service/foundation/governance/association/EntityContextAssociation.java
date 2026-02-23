package com.dekra.service.foundation.governance.association;

import com.dekra.service.foundation.domaincore.value.IdValue;
import lombok.Data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
public class EntityContextAssociation {
    private final IdValue entityId; // ID de la entidad principal
    private final String entityType; // Tipo de la entidad (e.g., "TEMPLATE", "FORM")
    private final Set<IdValue> contextIds; // Contextos asociados

    public EntityContextAssociation(IdValue entityId, String entityType) {
        this.entityId = Objects.requireNonNull(entityId, "Entity ID cannot be null");
        this.entityType = Objects.requireNonNull(entityType, "Entity type cannot be null");
        this.contextIds = new HashSet<>();
    }

    public void addContext(IdValue contextId) {
        this.contextIds.add(Objects.requireNonNull(contextId, "Context ID cannot be null"));
    }

    public void removeContext(IdValue contextId) {
        this.contextIds.remove(contextId);
    }

    public boolean isAssociatedWithContext(IdValue contextId) {
        return this.contextIds.contains(contextId);
    }
}
