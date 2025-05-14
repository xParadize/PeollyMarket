package com.peolly.ordermicroservice.dto;

import java.io.Serializable;

public record AddToCartDto(
        Long itemId,
        int quantity)
implements Serializable {
}
