package com.paravai.communities.composition.offer.port;

import java.util.Set;

public record CommunitySummary(
        String id,
        Set<String> allowedExchangeTypes
) {
    public boolean allows(String exchangeType) {
        return allowedExchangeTypes != null && allowedExchangeTypes.contains(exchangeType);
    }
}