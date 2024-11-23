package com.peolly.utilservice.events;

import java.io.Serializable;

public record WasPaymentMethodDeletedEvent(
        boolean success)
implements Serializable {
}
