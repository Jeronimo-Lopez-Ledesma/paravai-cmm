package com.paravai.foundation.localization;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class I18nConfig {

    @Bean
    public MessageService messageService(MessageSource messageSource) {
        return new MessageService(messageSource);
    }
}