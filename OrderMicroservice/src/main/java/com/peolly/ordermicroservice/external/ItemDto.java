package com.peolly.ordermicroservice.external;

import java.io.Serializable;

public record ItemDto(
        Long id,
        String name,
        String description,
        String image,
        Double price,
        Integer inStockQuantity
) implements Serializable {
}
