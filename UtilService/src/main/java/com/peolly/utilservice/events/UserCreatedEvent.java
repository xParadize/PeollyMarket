package com.peolly.utilservice.events;

import java.io.Serializable;

public record UserCreatedEvent (
    String userToken,
    String email,
    String username)
implements Serializable {
}
