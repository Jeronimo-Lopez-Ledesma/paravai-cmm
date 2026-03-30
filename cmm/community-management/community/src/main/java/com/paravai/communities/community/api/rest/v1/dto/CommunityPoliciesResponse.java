package com.paravai.communities.community.api.rest.v1.dto;

import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.value.CommunityRulesValue;

import java.util.List;

public class CommunityPoliciesResponse {

    private String id;
    private String rules;
    private List<String> allowedExchangeTypes;
    private String policySummary;

    public static CommunityPoliciesResponse fromDomain(Community c) {
        CommunityPoliciesResponse r = new CommunityPoliciesResponse();

        r.id = c.id().value();
        r.rules = c.rules()
                .map(CommunityRulesValue::getText)
                .orElse(null);
        r.allowedExchangeTypes = c.allowedExchangeTypes();
        r.policySummary = c.policySummary().orElse(null);

        return r;
    }

    public String getId() { return id; }
    public String getRules() { return rules; }
    public List<String> getAllowedExchangeTypes() { return allowedExchangeTypes; }
    public String getPolicySummary() { return policySummary; }
}