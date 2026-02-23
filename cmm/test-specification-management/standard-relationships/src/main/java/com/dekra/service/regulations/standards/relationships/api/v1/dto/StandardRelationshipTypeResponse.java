package com.paravai.regulations.standards.relationships.api.v1.dto;

import com.paravai.foundation.localization.MessageService;
import com.paravai.regulations.standards.relationships.domain.value.StandardRelationshipTypeValue;

import java.util.Locale;

public final class StandardRelationshipTypeResponse {

    private String code;
    private String label;

    public static StandardRelationshipTypeResponse fromDomain(StandardRelationshipTypeValue v,
                                                              Locale locale,
                                                              MessageService msg) {
        StandardRelationshipTypeResponse r = new StandardRelationshipTypeResponse();
        r.code = v.getCode();

        // i18n key format: standards.relationshipType.<CODE>
        String key = "standards.relationshipType." + v.getCode();
        r.label = msg.get(key, locale);

        return r;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
