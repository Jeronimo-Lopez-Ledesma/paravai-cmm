package com.paravai.communities.composition.api.rest.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update offer availability")
public class UpdateOfferAvailabilityRequest {

    @NotBlank
    @Schema(description = "Availability status code", example = "UNAVAILABLE")
    private String availabilityStatusCode;

    public String getAvailabilityStatusCode() {
        return availabilityStatusCode;
    }

    public void setAvailabilityStatusCode(String availabilityStatusCode) {
        this.availabilityStatusCode = availabilityStatusCode;
    }
}