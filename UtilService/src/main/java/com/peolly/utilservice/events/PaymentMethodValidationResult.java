package com.peolly.utilservice.events;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentMethodValidationResult(
        boolean valid,
        UUID userId,
        String email,
        String cardNumber,
        LocalDateTime timestamp)
implements Serializable {
}
