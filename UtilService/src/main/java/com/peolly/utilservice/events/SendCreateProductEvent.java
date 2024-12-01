package com.peolly.utilservice.events;

import java.io.Serializable;

public record SendCreateProductEvent(
        String name,
        String description,
        Long companyId,
        Double price)
implements Serializable {
}
