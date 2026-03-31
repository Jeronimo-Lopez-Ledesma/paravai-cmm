package com.paravai.communities.composition.offer.port;

public record MembershipSummary(
        String id,
        String statusCode
) {
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(statusCode);
    }
}