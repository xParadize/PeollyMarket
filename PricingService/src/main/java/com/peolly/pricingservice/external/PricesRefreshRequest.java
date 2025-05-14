package com.peolly.pricingservice.external;

public record PricesRefreshRequest (
        Long itemId,
        int quantity,
        double currentPrice
) {
}