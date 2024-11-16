package com.peolly.utilservice.events;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SendUserCreatedEvent {
    private String userToken;
    private String email;
    private String username;
}
