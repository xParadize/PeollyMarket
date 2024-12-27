package com.peolly.utilservice.events;

import java.io.Serializable;
import java.util.List;

public record GetAllPaymentMethodsEvent(
        List<String> paymentMethods)
implements Serializable {
}
