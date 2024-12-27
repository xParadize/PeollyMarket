package com.peolly.utilservice.events;

import java.io.Serializable;
import java.util.UUID;

public record DeletePaymentMethodEvent(
        UUID userId,
        String cardNumber)
implements Serializable {
}
