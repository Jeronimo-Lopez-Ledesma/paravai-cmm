package com.paravai.foundation.localization;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class LocaleContextTest {

    // Test unitario directo sobre método auxiliar
    @Test
    void shouldReturnLocaleFromGivenContext() {
        Locale locale = Locale.FRENCH;
        Context ctx = Context.of(LocaleContext.LOCALE_KEY, locale);

        Locale result = LocaleContext.getOrDefault(ctx);

        assertEquals("fr", result.getLanguage());
    }

    @Test
    void shouldReturnDefaultLocaleWhenNotInContext() {
        Context ctx = Context.empty();

        Locale result = LocaleContext.getOrDefault(ctx);

        assertEquals("en", result.getLanguage());
    }

    // Test reactivo para método real que accede al contexto global
    @Test
    void shouldReturnLocaleFromReactiveContext() {
        Locale locale = Locale.ITALIAN;

        Mono<Locale> result = Mono.deferContextual(ctx -> {
            Locale resolved = LocaleContext.getOrDefault(ctx);
            return Mono.just(resolved);
        }).contextWrite(Context.of(LocaleContext.LOCALE_KEY, locale));

        assertEquals("it", result.block().getLanguage());
    }


    @Test
    void shouldReturnDefaultLocaleWhenReactiveContextIsEmpty() {
        Mono<Locale> result = Mono.fromCallable(LocaleContext::getLocale);

        assertEquals("en", result.block().getLanguage());
    }
}
