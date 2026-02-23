package com.dekra.service.foundation.viewjsonapi.jsonapi;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;
import java.util.Map;

/**
 * Generic JSON:API-compliant response structure for paginated or single resources
 */
@Getter
@Builder
public class JsonApiResponse<T> {
    private final List<JsonApiResource<T>> data;
    private final Map<String, Object> meta;
    @Singular("link")
    private final Map<String, String> links;
}
