package com.dekra.service.foundation.viewjsonapi.jsonapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonApiData<T> {
    @NotBlank
    private String type;

    @NotNull
    @Valid
    private T attributes;
}
