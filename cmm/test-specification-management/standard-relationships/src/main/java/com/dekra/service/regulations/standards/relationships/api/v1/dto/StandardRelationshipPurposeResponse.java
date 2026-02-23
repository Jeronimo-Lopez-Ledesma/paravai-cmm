package com.paravai.regulations.standards.relationships.api.v1.dto;

import com.paravai.foundation.localization.MessageService;
import com.paravai.regulations.standards.relationships.domain.value.StandardRelationshipPurposeValue;

import java.util.Locale;

public final class StandardRelationshipPurposeResponse {

    private String code;
    private String label;

    public static StandardRelationshipPurposeResponse fromDomain(StandardRelationshipPurposeValue v,
                                                                 Locale locale,
                                                                 MessageService msg) {
        StandardRelationshipPurposeResponse r = new StandardRelationshipPurposeResponse();
        r.code = v.getCode();

        // i18n key format: standards.relationshipPurpose.<CODE>
        String key = "standards.relationshipPurpose." + v.getCode();
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
