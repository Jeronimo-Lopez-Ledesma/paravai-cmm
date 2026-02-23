package com.paravai.regulations.standards.api.rest.v1.dto;

import com.paravai.foundation.localization.MessageService;
import com.paravai.regulations.standards.domain.value.StandardVersionStatusValue;

import java.util.Locale;

public final class StandardVersionStatusResponse {

    private String code;
    private String label;

    public static StandardVersionStatusResponse fromDomain(StandardVersionStatusValue v,
                                                           Locale locale,
                                                           MessageService msg) {
        StandardVersionStatusResponse r = new StandardVersionStatusResponse();
        r.code = v.getCode();

        // i18n key format: standards.standardStatus.<CODE>
        String key = "standards.standardStatus." + v.getCode();
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
