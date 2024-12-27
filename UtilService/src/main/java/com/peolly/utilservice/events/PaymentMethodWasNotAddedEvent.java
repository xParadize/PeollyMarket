package com.peolly.utilservice.events;

import java.io.Serializable;

public record PaymentMethodWasNotAddedEvent(
        boolean successful,
        boolean isExpirationFieldsValid,
        boolean isBalanceSufficient,
        boolean isCardNotInUse)
implements Serializable {
}
