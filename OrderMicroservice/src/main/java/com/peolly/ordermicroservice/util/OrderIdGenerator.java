package com.peolly.ordermicroservice.util;

import org.springframework.stereotype.Component;

@Component
public class OrderIdGenerator {
    /**
     * Generates a random order ID within a specified range.
     *
     * @return a randomly generated order ID as a Long value.
     */
    public static Long generate() {
        Long orderId = (long) getRandomNumber(100000, 999999);
        return orderId;
    }

    /**
     * Generates a random integer within the specified range.
     *
     * @param min the minimum bound (inclusive).
     * @param max the maximum bound (exclusive).
     * @return a random integer between min (inclusive) and max (exclusive).
     */
    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}