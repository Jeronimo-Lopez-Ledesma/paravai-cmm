package com.paravai.communities.composition.offer.port;

public record ResourceSummary(
        String id,
        String ownerId,
        String title
) {
    public boolean isOwnedBy(String userId) {
        return ownerId != null && ownerId.equals(userId);
    }
}