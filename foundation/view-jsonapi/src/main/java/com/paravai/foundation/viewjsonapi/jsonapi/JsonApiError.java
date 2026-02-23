package com.paravai.foundation.viewjsonapi.jsonapi;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JsonApiError {
    private final int code;
    private final String message;
    private final List<DetailsError> details;
}