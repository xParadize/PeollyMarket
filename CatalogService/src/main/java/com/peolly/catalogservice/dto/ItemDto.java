package com.peolly.catalogservice.dto;

import java.io.Serializable;

public record ItemDto(
        Long id,
        String name,
        String description,
        String image,
        Double price,
        Integer inStockQuantity)
implements Serializable {
}
