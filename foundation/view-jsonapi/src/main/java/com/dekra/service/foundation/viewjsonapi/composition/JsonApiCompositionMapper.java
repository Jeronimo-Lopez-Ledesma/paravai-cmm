package com.dekra.service.foundation.viewjsonapi.composition;

import com.fasterxml.jackson.databind.JsonNode;


public interface JsonApiCompositionMapper<R> {

    /**
     * Converts a JSON:API composition document into a read model.
     * @param document The full JSON:API JSON tree (data + included).
     * @return A fully mapped read model instance.
     */
    R map(JsonNode document);
}
