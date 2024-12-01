package com.peolly.securityserver.usermicroservice.dto;

import java.io.Serializable;

public record RoleUpdateRequest(
        String role,
        boolean add)
implements Serializable {
}
