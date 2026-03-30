package com.paravai.communities.membership.api.rest.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to reject a membership request")
public class RejectMembershipRequest {

    @Schema(description = "Optional rejection reason", example = "Community is currently limited to internal members")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}