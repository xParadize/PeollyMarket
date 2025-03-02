package com.peolly.paymentmicroservice.exceptions;

public class CardNotExistsException extends RuntimeException {
    public CardNotExistsException(String message) {
        super(message);
    }
}
