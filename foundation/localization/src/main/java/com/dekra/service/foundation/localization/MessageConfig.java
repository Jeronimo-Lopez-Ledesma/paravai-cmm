package com.dekra.service.foundation.localization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Arrays;

@Configuration
public class MessageConfig {

    @Bean
    public MessageSource messageSource(
            @Value("${spring.messages.basename:i18n/messages}") String basenames,
            @Value("${spring.messages.encoding:UTF-8}") String encoding,
            @Value("${spring.messages.fallback-to-system-locale:false}") boolean fallback
    ) {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        String[] names = Arrays.stream(basenames.split(","))
                .map(String::trim)
                .map(s -> s.startsWith("classpath:") ? s : "classpath:" + s)
                .toArray(String[]::new);
        source.setBasenames(names);
        source.setDefaultEncoding(encoding);
        source.setFallbackToSystemLocale(fallback);
        return source;
    }
}