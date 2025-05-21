package com.peolly.securityserver.usermicroservice.dto;

import java.io.Serializable;

public record RoleUpdateRequest(
        String role,
        boolean isAdd)
implements Serializable {
}
