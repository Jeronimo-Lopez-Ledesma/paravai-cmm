package com.paravai.communities.community.api.rest.v1.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * REST DTO: ChangeCommunityVisibilityRequest (EPIC A / A2)
 *
 * MVP:
 * - visibilityCode required (PUBLIC | PRIVATE)
 */
@Getter
public class ChangeCommunityVisibilityRequest {

    /**
     * Catalog code for community visibility (PUBLIC | PRIVATE).
     */
    @NotBlank
    private String visibilityCode;

}