package com.peolly.pricingservice.external;

public record PricesRefreshResponse (
        Long itemId,
        double updatedPrice
) {
}