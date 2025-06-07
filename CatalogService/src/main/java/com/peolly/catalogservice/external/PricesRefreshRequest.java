package com.peolly.catalogservice.external;

public record PricesRefreshRequest (
        Long itemId,
        int quantity,
        double currentPrice
) {
}