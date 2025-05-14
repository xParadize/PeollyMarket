package com.peolly.storageservice.exceptions;

public class EmptyProductStorageAmountException extends RuntimeException {
    public EmptyProductStorageAmountException(String message) {
        super(message);
    }
}