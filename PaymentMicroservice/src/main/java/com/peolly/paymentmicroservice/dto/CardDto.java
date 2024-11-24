package com.peolly.paymentmicroservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CardDto {
    private String cardNumber;
    private String monthExpiration;
    private String yearExpiration;
    private int cvv;
    private Double availableMoney;
}
