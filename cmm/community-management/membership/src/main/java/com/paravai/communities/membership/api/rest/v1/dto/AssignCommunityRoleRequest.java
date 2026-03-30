package com.paravai.communities.membership.api.rest.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to assign or change a member role")
public class AssignCommunityRoleRequest {

    @NotBlank
    @Schema(description = "Role code to assign", example = "ADMIN")
    private String roleCode;

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }
}