package com.peolly.paymentmicroservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = "UncheckedCard", timeToLive = 900) // 15 mins
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UncheckedCard implements Serializable {

    @Id
    private String cardNumber;

    private String monthExpiration;
    private String yearExpiration;
    private int cvv;
    private Double availableMoney;
}
