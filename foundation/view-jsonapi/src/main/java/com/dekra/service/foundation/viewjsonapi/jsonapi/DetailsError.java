package com.paravai.foundation.viewjsonapi.jsonapi;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DetailsError {
    private final String field;
    private final String issue;
}
