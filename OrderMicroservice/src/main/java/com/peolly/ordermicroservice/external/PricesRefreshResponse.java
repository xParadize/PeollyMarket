package com.peolly.ordermicroservice.external;

public record PricesRefreshResponse (
        Long itemId,
        double updatedPrice
) {
}