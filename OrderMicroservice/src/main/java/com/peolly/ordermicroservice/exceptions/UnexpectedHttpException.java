package com.peolly.ordermicroservice.exceptions;

public class UnexpectedHttpException extends RuntimeException {
    public UnexpectedHttpException(String message) {
        super(message);
    }
}
