package com.dekra.service.foundation.domaincore.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public final class EntityTypeValue {

    public static final EntityTypeValue ORGANIZATION = new EntityTypeValue("Organization");
    public static final EntityTypeValue LOCATION = new EntityTypeValue("Location");
    public static final EntityTypeValue SERVICE = new EntityTypeValue("Services");
    public static final EntityTypeValue SERVICE_FAMILY = new EntityTypeValue("Service Family");
    public static final EntityTypeValue PRODUCT = new EntityTypeValue("Product");
    public static final EntityTypeValue BUSINESS_LINE = new EntityTypeValue("BusinessLine");
    public static final EntityTypeValue CONTACT = new EntityTypeValue("Contact");
    public static final EntityTypeValue CERTIFICATE = new EntityTypeValue("Certificate");
    public static final EntityTypeValue OPPORTUNITY = new EntityTypeValue("Opportunity");
    public static final EntityTypeValue STANDARD = new EntityTypeValue("Standard");
    public static final EntityTypeValue STANDARD_RELATIONSHIP = new EntityTypeValue("Standard Relationship");
    public static final EntityTypeValue TEST_SPECIFICATION = new EntityTypeValue("Test Specification");
    public static final EntityTypeValue TEST_CASE = new EntityTypeValue("Test Case");

    private final String value;

    private EntityTypeValue(String value) {
        this.value = value;
    }

    @JsonCreator
    public static EntityTypeValue of(String value) {
        return switch (value.trim().toLowerCase()) {
            case "Organization" -> ORGANIZATION;
            case "Location" -> LOCATION;
            case "Service" -> SERVICE;
            case "Service Familiy" -> SERVICE_FAMILY;
            case "Product" -> PRODUCT;
            case "Business-line" -> BUSINESS_LINE;
            case "Contact" -> CONTACT;
            case "Certificate" -> CERTIFICATE;
            case "Opportunities" -> OPPORTUNITY;
            case "Standard" -> STANDARD;
            case "Standard Relationship" -> STANDARD_RELATIONSHIP;
            case "Test Specification" -> TEST_SPECIFICATION;
            case "Test Case" -> TEST_CASE;
            default -> throw new IllegalArgumentException("Unsupported entity type: " + value);
        };
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return (this == o) || (o instanceof EntityTypeValue other && value.equals(other.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
