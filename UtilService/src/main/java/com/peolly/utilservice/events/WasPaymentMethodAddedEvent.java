package com.peolly.utilservice.events;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WasPaymentMethodAddedEvent {
    private boolean successful;
}
