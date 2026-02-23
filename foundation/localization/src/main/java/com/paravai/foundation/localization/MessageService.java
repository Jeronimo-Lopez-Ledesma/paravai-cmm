package com.paravai.foundation.localization;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service("messsageservice")
@Primary
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String code) {
        return get(code, LocaleContext.getLocale());
    }

    public String get(String code, Locale locale) {
        return messageSource.getMessage(code, null, code, locale);
    }

    public String get(String code, Object[] args, String defaultMessage, Locale locale) {
        return messageSource.getMessage(code, args, defaultMessage, locale);
    }
}
