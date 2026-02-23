package com.paravai.foundation.governance.resilience;

/**
 * Centralizes naming conventions for Circuit Breakers, Retries, Timeouts,
 * and other Resilience4j policies used across the DEKRA Shared Services Platform.
 *
 * Naming rules:
 *   - ALWAYS lower-kebab-case
 *   - Prefix by domain: webclient, mongo, kafka, composition, readmodel
 *   - Format for WebClient:
 *       webclient.{serviceName}.cb
 *       webclient.{serviceName}.retry
 *
 *   - Format for other technical adapters:
 *       mongo.{collection}.cb
 *       kafka.{topic}.cb
 *
 *   - Format for orchestrators:
 *       composition.{resource}.cb
 *
 * extractServiceName() allows metrics tagging by extracting "{serviceName}"
 * from any breaker/retry name.
 */
public final class BreakerNameFactory {

    private BreakerNameFactory() {
        // Utility class
    }

    // ============================================================
    // FACTORY METHODS
    // ============================================================

    /** Standard CB name for downstream WebClient calls */
    public static String webClientCB(String serviceName) {
        return "webclient." + normalize(serviceName) + ".cb";
    }

    public static String webClientRetry(String serviceName) {
        return "webclient." + normalize(serviceName) + ".retry";
    }

    /** Orchestrator breaker for a composed resource (e.g., opportunities) */
    public static String compositionCB(String resource) {
        return "composition." + normalize(resource) + ".cb";
    }

    /** ReadModel breaker */
    public static String readModelCB(String resource) {
        return "readmodel." + normalize(resource) + ".cb";
    }

    /** Mongo breaker, grouped by collection name */
    public static String mongoCB(String collection) {
        return "mongo." + normalize(collection) + ".cb";
    }

    // ============================================================
    // UTILS
    // ============================================================

    /**
     * Extracts the "{serviceName}" part from breaker names like:
     *   - webclient.opportunities.cb   → opportunities
     *   - webclient.contacts.retry     → contacts
     *   - composition.opportunities.cb → opportunities
     *
     * If the name does not match the expected pattern, returns the full name.
     */
    public static String extractServiceName(String breakerName) {
        if (breakerName == null) return "unknown";

        // Split by '.'
        String[] parts = breakerName.split("\\.");

        // Expected patterns:
        // webclient.{service}.cb
        // webclient.{service}.retry
        // composition.{resource}.cb
        // readmodel.{resource}.cb
        //
        // So at least 3 parts: prefix / serviceName / suffix
        if (parts.length >= 3) {
            return parts[1]; // Extract the service/resource name
        }

        return breakerName; // fallback
    }

    private static String normalize(String value) {
        return value.toLowerCase().replace("_", "-").replace(" ", "-");
    }
}
