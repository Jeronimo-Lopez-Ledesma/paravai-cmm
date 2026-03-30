package com.paravai.communities.membership.api.rest.v1.dto;

import com.paravai.communities.membership.application.query.getmy.GetMyMembershipResult;

public class MyMembershipResponse {

    private String status;
    private String role;

    public static MyMembershipResponse fromResult(GetMyMembershipResult r) {
        MyMembershipResponse resp = new MyMembershipResponse();

        resp.status = r.status().name();

        resp.role = r.role()
                .map(role -> role.getCode())
                .orElse(null);

        return resp;
    }

    public String getStatus() { return status; }
    public String getRole() { return role; }
}