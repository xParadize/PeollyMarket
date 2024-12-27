package com.peolly.securityserver.usermicroservice.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.CreditCardNumber;

@Data
public class CardData {
    @CreditCardNumber(message = "Invalid Card Number")
    private String cardNumber;

    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Incorrect Month Expiration")
    private String monthExpiration;

    @Pattern(regexp = "^([0-9][0-9])$", message = "Incorrect Month Expiration")
    private String yearExpiration;

    @Digits(integer = 3, fraction = 0, message = "Invalid CVV")
    private int cvv;

    private Double availableMoney;
}
