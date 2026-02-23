package com.paravai.foundation.viewjsonapi.jsonapi;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@Getter
@Builder
public class JsonApiSingleResponse<T> {
    private final JsonApiResource<T> data;

    @Singular("link")
    private final Map<String, String> links;

    private final Map<String, Object> meta;
}
