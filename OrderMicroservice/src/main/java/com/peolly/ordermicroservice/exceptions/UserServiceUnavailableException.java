package com.peolly.ordermicroservice.exceptions;

public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException(String message) {
        super(message);
    }
}
