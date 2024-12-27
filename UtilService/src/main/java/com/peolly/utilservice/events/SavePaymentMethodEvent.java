package com.peolly.utilservice.events;

import java.io.Serializable;
import java.util.UUID;

public record SavePaymentMethodEvent (
    UUID userId,
    String cardNumber,
    String monthExpiration,
    String yearExpiration,
    int cvv,
    Double availableMoney)
implements Serializable {
}
