package com.paravai.foundation.domain.value;

/** Normaliza EntityTypeValue a ResourceTypeValue */
public final class ResourceTypeMapper {

    private ResourceTypeMapper(){}

    public static ResourceTypeValue fromEntityType(EntityTypeValue entityType) {
        final String v = entityType.getValue().trim().toLowerCase();

        return switch (v) {
            case "organization", "organizations" -> ResourceTypeValue.ORGANIZATIONS;
            case "location", "locations"         -> ResourceTypeValue.LOCATIONS;
            case "service", "services"           -> ResourceTypeValue.SERVICES;
            case "service family", "service-family", "service-families", "service-familiy"
                    -> ResourceTypeValue.SERVICE_FAMILIES;
            case "product", "products"           -> ResourceTypeValue.PRODUCTS;
            case "business line", "business-line","business-lines"
                    -> ResourceTypeValue.BUSINESS_LINES;
            default -> throw new IllegalArgumentException(
                    "Unsupported mapping from EntityTypeValue to ResourceTypeValue: " + entityType
            );
        };
    }
}
