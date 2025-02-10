package com.peolly.ordermicroservice.util;

import org.springframework.stereotype.Component;

@Component
public class OrderIdGenerator {
    public static Long generate() {
        Long orderId = (long) getRandomNumber(100000, 999999);
        return orderId;
    }

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}