package com.paravai.communities.membership.api.rest.v1.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * REST DTO: InviteMemberRequest (EPIC A / A4)
 *
 * MVP:
 * - inviteeUserId required (userId)
 */
@Getter
public class InviteMemberRequest {

    @NotBlank
    private String inviteeUserId;
}