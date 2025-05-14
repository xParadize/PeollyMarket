package com.peolly.ordermicroservice.external;

public record PricesRefreshRequest (
        Long itemId,
        int quantity,
        double currentPrice
) {
}
