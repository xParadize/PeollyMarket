package com.peolly.securityserver.securityserver;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}
