package com.peolly.ordermicroservice.exceptions;

public class ItemReservationException extends RuntimeException {
    public ItemReservationException(String message) {
        super(message);
    }
}
