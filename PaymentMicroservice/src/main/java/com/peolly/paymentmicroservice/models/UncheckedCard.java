package com.peolly.paymentmicroservice.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash(value = "UncheckedCard", timeToLive = 900) // 15 mins
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
