package com.paravai.foundation.localization;

import java.util.Locale;

/**
 * Interface for Value Objects that expose a human-readable label
 * that can be resolved in a specific language/locale using MessageService.
 *
 * Typical use cases include enums, statuses, categories, and other constant-based VOs.
 */
public interface LocalizableValueObject {

    /**
     * Returns the localized label associated with this value object,
     * using the provided locale and message service.
     *
     * @param locale the locale to use for translation (e.g. Locale.ENGLISH)
     * @param messageService the service used to resolve the translation key
     * @return the localized label string (or fallback key if not found)
     */
    String getLocalizedLabel(Locale locale, MessageService messageService);
}

