package com.paravai.foundation.domaincore.value;

import com.paravai.foundation.localization.LocalizableValueObject;
import com.paravai.foundation.localization.MessageService;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;
import java.util.Objects;

public final class OperationTypeValue implements LocalizableValueObject {

    public static final OperationTypeValue CREATED = new OperationTypeValue("created");
    public static final OperationTypeValue UPDATED = new OperationTypeValue("updated");
    public static final OperationTypeValue DELETED = new OperationTypeValue("deleted");

    private final String value;

    private OperationTypeValue(String value) {
        this.value = Objects.requireNonNull(value.toLowerCase()).trim();
    }

    @JsonCreator
    public static OperationTypeValue of(String value) {
        return switch (value.trim().toLowerCase()) {
            case "created" -> CREATED;
            case "updated" -> UPDATED;
            case "deleted" -> DELETED;
            default -> throw new IllegalArgumentException("Unsupported operation type: " + value);
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

    // equals and hashCode are important for Value Objects
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperationTypeValue that)) return false;
        return value.equalsIgnoreCase(that.value);
    }

    @Override
    public int hashCode() {
        return value.toLowerCase().hashCode();
    }

    @Override
    public String getLocalizedLabel(Locale locale, MessageService messageService) {
        return messageService.get("operationType." + value.toLowerCase(locale), locale);
    }

    public boolean isCreate() {
        return this.equals(CREATED);
    }

    public boolean isUpdate() {
        return this.equals(UPDATED);
    }

    public boolean isDelete() {
        return this.equals(DELETED);
    }

}

