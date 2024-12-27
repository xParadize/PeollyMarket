package com.peolly.productmicroservice.dto;

import java.io.Serializable;

public record ProductDto(
        String name,
        String description,
        Long companyId,
        Double price)
implements Serializable {
}
