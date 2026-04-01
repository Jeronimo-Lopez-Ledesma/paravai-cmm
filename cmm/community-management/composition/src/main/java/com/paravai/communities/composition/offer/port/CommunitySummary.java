package com.paravai.communities.composition.offer.port;

import java.util.Set;

public record CommunitySummary(
        String communityId,
        Set<String> allowedExchangeTypes
) {

    public boolean allowsExchangeType(String exchangeType) {
        if (exchangeType == null || exchangeType.isBlank()) {
            return false;
        }

        if (allowedExchangeTypes == null || allowedExchangeTypes.isEmpty()) {
            return true;
        }

        return allowedExchangeTypes.contains(exchangeType);
    }
}