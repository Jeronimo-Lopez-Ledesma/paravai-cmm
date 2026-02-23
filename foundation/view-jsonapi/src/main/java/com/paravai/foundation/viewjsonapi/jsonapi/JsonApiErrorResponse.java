package com.paravai.foundation.viewjsonapi.jsonapi;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JsonApiErrorResponse {
    private final JsonApiError error;
}
