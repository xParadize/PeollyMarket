package com.peolly.paymentmicroservice.models;

public record CardValidationErrorFields (
        boolean isCardDataValid,
        boolean isExpirationFieldsValid,
        boolean isBalanceInsufficient,
        boolean isCardAlreadyInUse) {
}
