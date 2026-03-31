package com.paravai.communities.composition.api.rest.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to publish a new offer")
public class PublishOfferRequest {

    @NotBlank
    @Schema(description = "Community identifier where the offer will be published", example = "community-123")
    private String communityId;

    @NotBlank
    @Schema(description = "Resource identifier owned by the authenticated user", example = "resource-456")
    private String resourceId;

    @NotBlank
    @Schema(description = "Exchange type code", example = "LEND")
    private String exchangeTypeCode;

    @Schema(description = "Optional free-text offer description", example = "Available on weekends")
    private String description;

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getExchangeTypeCode() {
        return exchangeTypeCode;
    }

    public void setExchangeTypeCode(String exchangeTypeCode) {
        this.exchangeTypeCode = exchangeTypeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}