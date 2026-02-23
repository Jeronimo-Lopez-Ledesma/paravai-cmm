package com.dekra.service.foundation.viewjsonapi.jsonapi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsonApiRequest<T> {
    @NotNull
    @Valid
    private JsonApiData<T> data;
}
