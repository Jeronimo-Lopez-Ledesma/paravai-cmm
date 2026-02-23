package com.paravai.regulations.standards.domain.model;

import com.paravai.foundation.domain.organization.value.OrganizationAssociationValue;
import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.foundation.domaincore.value.TimestampValue;
import com.paravai.regulations.standards.domain.value.StandardCodeValue;
import com.paravai.regulations.standards.domain.value.StandardTitleValue;
import com.paravai.regulations.standards.domain.value.StandardTypeValue;

import java.util.List;
import java.util.Objects;

public final class StandardFactory {

    private StandardFactory() {
        throw new IllegalStateException("Factory class");
    }

    public static Standard create(StandardCodeValue code,
                                  StandardTitleValue title,
                                  StandardTypeValue type,
                                  StandardVersion initialVersion,
                                  String description,
                                  OrganizationAssociationValue issuingBody,
                                  TimestampValue now) {

        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(initialVersion, "initialVersion");
        Objects.requireNonNull(issuingBody, "issuingBody");
        Objects.requireNonNull(now, "now");

        return new Standard(
                IdValue.generate(),
                code,
                title,
                type,
                List.of(initialVersion),
                now,
                now,
                description,
                issuingBody
        );
    }

    public static Standard recreate(IdValue id,
                                    StandardCodeValue code,
                                    StandardTitleValue title,
                                    StandardTypeValue type,
                                    List<StandardVersion> versions,
                                    TimestampValue createdAt,
                                    TimestampValue updatedAt,
                                    String description,
                                    OrganizationAssociationValue issuingBody) {

        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(versions, "versions");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
        Objects.requireNonNull(issuingBody, "issuingBody");

        return new Standard(
                id,
                code,
                title,
                type,
                versions,
                createdAt,
                updatedAt,
                description,
                issuingBody
        );
    }
}