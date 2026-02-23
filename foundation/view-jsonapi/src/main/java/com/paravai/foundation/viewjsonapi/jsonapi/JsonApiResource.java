package com.paravai.foundation.viewjsonapi.jsonapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Builder
class JsonApiResource<T> {
    private final String id;
    private final String type;
    private final T attributes;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, Object> relationships;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Singular("link")
    private final Map<String, String> links;

}

