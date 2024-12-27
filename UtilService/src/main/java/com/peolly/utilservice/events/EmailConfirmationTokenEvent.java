package com.peolly.utilservice.events;

import java.util.UUID;

public record EmailConfirmationTokenEvent (
    UUID tempUserTokenId,
    String email) {
}
