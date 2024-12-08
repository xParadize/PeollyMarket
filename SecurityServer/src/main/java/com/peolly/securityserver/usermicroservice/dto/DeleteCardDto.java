package com.peolly.securityserver.usermicroservice.dto;

import java.io.Serializable;

public record DeleteCardDto(
        String cardNumber)
implements Serializable {
}
