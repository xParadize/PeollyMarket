package com.peolly.utilservice.events;

import java.io.Serializable;
import java.util.UUID;

public record UserIdEvent(
        UUID userId)
implements Serializable {
}
