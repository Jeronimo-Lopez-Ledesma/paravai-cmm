package com.paravai.communities.resource.api.rest.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to register a new resource")
public class RegisterResourceRequest {

    @NotBlank
    @Schema(description = "Resource title", example = "Cordless drill")
    private String title;

    @Schema(description = "Optional free-text description", example = "Bosch cordless drill in good condition")
    private String description;

    @Schema(description = "Optional resource condition code", example = "GOOD")
    private String conditionCode;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConditionCode() {
        return conditionCode;
    }

    public void setConditionCode(String conditionCode) {
        this.conditionCode = conditionCode;
    }
}