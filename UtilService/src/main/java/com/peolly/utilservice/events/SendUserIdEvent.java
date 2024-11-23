package com.peolly.utilservice.events;

import java.io.Serializable;
import java.util.UUID;

public record SendUserIdEvent(
        UUID userId)
implements Serializable {
}
