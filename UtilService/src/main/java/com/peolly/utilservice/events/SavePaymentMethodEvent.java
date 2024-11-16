package com.peolly.utilservice.events;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SavePaymentMethodEvent {
    private UUID userId;
    private String cardNumber;
    private String monthExpiration;
    private String yearExpiration;
    private int cvv;
    private Double availableMoney;
}
