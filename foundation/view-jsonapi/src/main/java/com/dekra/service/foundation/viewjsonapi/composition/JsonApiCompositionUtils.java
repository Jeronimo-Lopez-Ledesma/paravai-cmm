package com.dekra.service.foundation.viewjsonapi.composition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public final class JsonApiCompositionUtils {

    private JsonApiCompositionUtils() {
        // Prevent instantiation
    }

    public static JsonNode emptyObject() {
        return JsonNodeFactory.instance.objectNode();
    }

    public static boolean isNullOrMissing(JsonNode n) {
        return n == null || n.isMissingNode() || n.isNull();
    }

    public static JsonNode safe(JsonNode n) {
        return isNullOrMissing(n) ? emptyObject() : n;
    }
}
