package com.paravai.foundation.domain.value;

/** Normaliza EntityTypeValue a ResourceTypeValue */
public final class ResourceTypeMapper {

    private ResourceTypeMapper(){}

    public static ResourceTypeValue fromEntityType(EntityTypeValue entityType) {
        final String v = entityType.value().trim().toLowerCase();

        return switch (v) {
            case "community", "communities" -> ResourceTypeValue.COMMUNITIES;
            case "membership", "memberships"         -> ResourceTypeValue.MEMBERSHIPS;
            default -> throw new IllegalArgumentException(
                    "Unsupported mapping from EntityTypeValue to ResourceTypeValue: " + entityType
            );
        };
    }
}
