package com.peolly.ordermicroservice.dto;

import java.io.Serializable;

public record RemoveFromCartDto(
        Long productId)
implements Serializable {
}
