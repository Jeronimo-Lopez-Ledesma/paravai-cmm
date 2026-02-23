package com.paravai.foundation.viewjsonapi.jsonapi;

import java.util.List;

public class JsonApiErrorFactory {
    public static JsonApiErrorResponse create(int code, String message) {
        return JsonApiErrorResponse.builder()
                .error(JsonApiError.builder()
                        .code(code)
                        .message(message)
                        .details(List.of())
                        .build())
                .build();
    }

    public static JsonApiErrorResponse create(int code, String message, List<DetailsError> details) {
        return JsonApiErrorResponse.builder()
                .error(JsonApiError.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .build();
    }

    public static DetailsError of(String field, String message) {
        return DetailsError.builder()
                .field(field)
                .issue(message)
                .build();
    }
}
