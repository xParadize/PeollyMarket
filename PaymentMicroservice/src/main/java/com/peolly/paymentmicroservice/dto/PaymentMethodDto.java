package com.peolly.paymentmicroservice.dto;

import java.io.Serializable;

public record PaymentMethodDto(
        String cardNumber)
implements Serializable {
}
