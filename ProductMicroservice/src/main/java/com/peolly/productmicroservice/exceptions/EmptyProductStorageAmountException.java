package com.peolly.productmicroservice.exceptions;

public class EmptyProductStorageAmountException extends RuntimeException {
    public EmptyProductStorageAmountException(String message) {
        super(message);
    }
}
