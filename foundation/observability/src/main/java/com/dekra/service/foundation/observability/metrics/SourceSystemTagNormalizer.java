package com.paravai.foundation.observability.metrics;

import java.util.Locale;

/**
 * Normalizes the "sourceSystem" header value for use as a low-cardinality metrics tag.
 *
 * <p>Governance of valid source systems is handled outside the codebase (e.g., Confluence/API contract).
 * This normalizer exists only as a safety net to prevent accidental high-cardinality tags in metrics.</p>
 *
 * <p>Strategy (defensive, no allowlist):
 * <ul>
 *   <li>null / blank -> "unknown"</li>
 *   <li>trim + lowercase</li>
 *   <li>max length limit</li>
 *   <li>allowed chars: [a-z0-9_-]</li>
 *   <li>otherwise -> "unknown"</li>
 * </ul>
 * </p>
 */
public final class SourceSystemTagNormalizer {

    private static final String UNKNOWN = "unknown";
    private static final int MAX_LEN = 24;

    public String normalize(String raw) {
        if (raw == null) return UNKNOWN;

        String v = raw.trim().toLowerCase(Locale.ROOT);
        if (v.isEmpty()) return UNKNOWN;
        if (v.length() > MAX_LEN) return UNKNOWN;

        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            boolean ok =
                    (c >= 'a' && c <= 'z')
                            || (c >= '0' && c <= '9')
                            || c == '-'
                            || c == '_';
            if (!ok) return UNKNOWN;
        }

        return v;
    }
}