package com.peolly.securityserver.usermicroservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CardData {
    private String cardNumber;
    private String monthExpiration;
    private String yearExpiration;
    private int cvv;
    private Double availableMoney;
}
