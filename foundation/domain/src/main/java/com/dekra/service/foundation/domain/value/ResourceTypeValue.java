package com.paravai.foundation.domain.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public final class ResourceTypeValue {

    public static final ResourceTypeValue ORGANIZATIONS = new ResourceTypeValue("organizations");
    public static final ResourceTypeValue LOCATIONS = new ResourceTypeValue("locations");
    public static final ResourceTypeValue SERVICES = new ResourceTypeValue("services");
    public static final ResourceTypeValue SERVICE_FAMILIES = new ResourceTypeValue("service-families");
    public static final ResourceTypeValue PRODUCTS = new ResourceTypeValue("products");
    public static final ResourceTypeValue BUSINESS_LINES = new ResourceTypeValue("business-lines");
    public static final ResourceTypeValue CONTACT = new ResourceTypeValue("contact");
    public static final ResourceTypeValue CERTIFICATES = new ResourceTypeValue("certificates");
    public static final ResourceTypeValue OPPORTUNITIES = new ResourceTypeValue("opportunities");
    public static final ResourceTypeValue STANDARDS = new ResourceTypeValue("standards");
    public static final ResourceTypeValue STANDARD_RELATIONSHIPS= new ResourceTypeValue("standard-relationships");
    public static final ResourceTypeValue TEST_SPECIFICATIONS = new ResourceTypeValue("test-specifications");
    public static final ResourceTypeValue TEST_CASES = new ResourceTypeValue("test-cases");



    private final String value;

    private ResourceTypeValue(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ResourceTypeValue of(String value) {
        return switch (value.trim().toLowerCase()) {
            case "organizations" -> ORGANIZATIONS;
            case "locations" -> LOCATIONS;
            case "services" -> SERVICES;
            case "service-families" -> SERVICE_FAMILIES;
            case "products" -> PRODUCTS;
            case "business-lines" -> BUSINESS_LINES;
            case "contact" -> CONTACT;
            case "certificates" -> CERTIFICATES;
            case "opportunities" -> OPPORTUNITIES;
            case "standards" -> STANDARDS;
            case "standard-relationbships" -> STANDARD_RELATIONSHIPS;
            case "test-specifications" -> TEST_SPECIFICATIONS;
            case "test-cases" -> TEST_CASES;

            default -> throw new IllegalArgumentException("Unsupported resource type: " + value);
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
        return (this == o) || (o instanceof ResourceTypeValue other && value.equals(other.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
