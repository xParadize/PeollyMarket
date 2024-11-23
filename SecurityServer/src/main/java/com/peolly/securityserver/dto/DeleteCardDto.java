package com.peolly.securityserver.dto;

import java.io.Serializable;

public record DeleteCardDto(
        String cardNumber)
implements Serializable {
}
