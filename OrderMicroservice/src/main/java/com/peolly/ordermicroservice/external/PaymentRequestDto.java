package com.peolly.ordermicroservice.external;

import java.util.UUID;

public record PaymentRequestDto (
        String cardNumber,
        UUID userId,
        double totalCost,
        Long orderId
) {
}