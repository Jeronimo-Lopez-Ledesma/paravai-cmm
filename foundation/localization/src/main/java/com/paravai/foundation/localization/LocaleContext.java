package com.paravai.foundation.localization;

import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.util.Locale;

/**
 * Utility class to manage the Locale context in a reactive environment.
 *
 * <p>This class provides access to the {@link Locale} that is propagated via Reactor's Context,
 * typically set by a WebFilter like {@link LocaleContextWebFilter} based on the Accept-Language header.
 *
 * <p>All services and components downstream can retrieve the locale using {@link #getLocale()},
 * or use {@link #getOrDefault(ContextView)} in tests or manually created contexts.
 */
public class LocaleContext {

    public static final String LOCALE_KEY = "locale";

    /**
     * Returns the current Locale from the Reactor context.
     * If not present, defaults to {@link Locale#ENGLISH}.
     *
     * <p>This method is blocking and should only be used in non-reactive code (e.g., DTO mapping).
     */
    public static Locale getLocale() {
        return reactor.core.publisher.Mono.deferContextual(Mono::just)
                .map(ctx -> ctx.getOrDefault(LOCALE_KEY, Locale.ENGLISH))
                .blockOptional()
                .orElse(Locale.ENGLISH);
    }

    /**
     * Returns the Locale from a given {@link ContextView}, defaulting to ENGLISH if not found.
     * <p>Useful for unit tests or manual context inspection.
     *
     * @param ctx the Reactor context view
     * @return the resolved Locale
     */
    public static Locale getOrDefault(ContextView ctx) {
        return ctx.getOrDefault(LOCALE_KEY, Locale.ENGLISH);
    }
}
