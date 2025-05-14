package com.peolly.paymentmicroservice.dto;

import java.util.UUID;

public record PaymentRequestDto(
    String cardNumber,
    UUID userId,
    double totalCost,
    Long orderId
) {

}