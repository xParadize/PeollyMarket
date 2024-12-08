package com.peolly.securityserver.securityserver.models;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
