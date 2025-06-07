package com.peolly.catalogservice.external;

public record PricesRefreshResponse (
        Long itemId,
        double updatedPrice
) {
}