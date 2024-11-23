package com.peolly.utilservice.events;

import java.io.Serializable;
import java.util.UUID;

public record SendDeletePaymentMethodEvent(
        UUID userId,
        String cardNumber)
implements Serializable {
}
