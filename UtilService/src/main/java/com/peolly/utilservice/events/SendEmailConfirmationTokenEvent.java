package com.peolly.utilservice.events;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SendEmailConfirmationTokenEvent {
    private UUID tempUserTokenId;
    private String email;
}
