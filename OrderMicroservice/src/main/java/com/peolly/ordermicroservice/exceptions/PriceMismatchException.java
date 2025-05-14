package com.peolly.ordermicroservice.exceptions;

public class PriceMismatchException extends RuntimeException {
    public PriceMismatchException(String message) {
        super(message);
    }
}
