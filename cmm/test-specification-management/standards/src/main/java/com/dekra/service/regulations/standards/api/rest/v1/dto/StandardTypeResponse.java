package com.dekra.service.regulations.standards.api.rest.v1.dto;

import com.dekra.service.foundation.localization.MessageService;
import com.dekra.service.regulations.standards.domain.value.StandardTypeValue;

import java.util.Locale;

public final class StandardTypeResponse {

    private String code;
    private String label;

    public static StandardTypeResponse fromDomain(StandardTypeValue v, Locale locale, MessageService msg) {
        StandardTypeResponse r = new StandardTypeResponse();
        r.code = v.getCode();

        // i18n key format: standards.standardType.<CODE>
        String key = "standards.standardType." + v.getCode();
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