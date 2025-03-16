package com.peolly.catalogservice.dto;

import java.io.Serializable;

public record ProductDto(
        Long id,
        String name,
        String description,
        String image,
        Double price,
        Long companyId)
implements Serializable {
}
