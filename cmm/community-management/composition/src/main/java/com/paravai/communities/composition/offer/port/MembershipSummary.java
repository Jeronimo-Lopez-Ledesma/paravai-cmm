package com.paravai.communities.composition.offer.port;

public record MembershipSummary(
        String membershipId,
        String statusCode,
        String roleCode
) {

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(statusCode);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(roleCode);
    }
}